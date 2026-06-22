package com.example.learningApp.service.vocab;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.NotificationType;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntradayVocabReminderJob {

    private final UserVocabProgressRepository progressRepo;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Chạy mỗi giờ để kiểm tra xem có ai cần ôn tập gấp không (Real-time SRS)
     */
    @Scheduled(cron = "0 0 * * * *") // Chạy vào đầu mỗi giờ
    @Transactional
    public void sendIntradayReminders() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm TOÀN BỘ các bản ghi đã quá hạn (kể cả những từ bị quá hạn từ lâu nếu
        // server từng bị tắt)
        List<UserVocabProgress> dueProgress = progressRepo.findAllByNextReviewAtBefore(now);

        if (dueProgress.isEmpty())
            return;

        // Nhóm theo user để gửi 1 thông báo tổng hợp
        Map<User, List<UserVocabProgress>> userGroups = dueProgress.stream()
                .collect(Collectors.groupingBy(UserVocabProgress::getUser));

        userGroups.forEach((user, progressList) -> {
            // Chỉ gửi nếu lâu rồi chưa gửi nhắc nhở (tránh spam, ít nhất cách nhau 4 tiếng)
            if (user.getLastReminderSentAt() == null || user.getLastReminderSentAt().isBefore(now.minusHours(4))) {

                String title = "Đã đến lúc ôn tập rồi! ⚡";
                String content = String.format("Bạn có %d từ vựng vừa đến hạn ôn tập. Học ngay để không quên nhé!",
                        progressList.size());

                notificationService.create(user, title, content, NotificationType.REVIEW_REMINDER, null);

                // Cập nhật thời gian gửi nhắc nhở cuối để không bị lặp lại trong phiên chạy sau
                user.setLastReminderSentAt(now);
                userRepository.save(user);

                log.info("Sent intraday reminder to user: {}", user.getEmail());
            }
        });
    }
}
