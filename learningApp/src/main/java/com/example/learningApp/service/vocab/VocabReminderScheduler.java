package com.example.learningApp.service.vocab;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VocabReminderScheduler {

    private final UserVocabProgressRepository progressRepo;
    private final NotificationService notificationService;

    // test: 10s | prod: 5–15 phút
    @Scheduled(fixedDelay = 10 * 1000)
    @Transactional
    public void remindForgottenVocabs() {

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // 🔥 chỉ tính vocab CHƯA THUỘC
        List<LearningStatus> NEED_REVIEW =
                List.of(LearningStatus.FORGOTTEN, LearningStatus.LEARNING);

        List<UserVocabProgress> dueList =
                progressRepo.findByLastReviewedAtLessThanEqualAndStatusIn(
                        threeDaysAgo,
                        NEED_REVIEW
                );

        // 🔥 lọc chống spam + group theo user
        Map<User, List<UserVocabProgress>> byUser =
                dueList.stream()
                        .filter(p ->
                                p.getLastReminderSentAt() == null ||
                                        p.getLastReminderSentAt().isBefore(oneDayAgo)
                        )
                        .collect(Collectors.groupingBy(
                                UserVocabProgress::getUser
                        ));

        for (var entry : byUser.entrySet()) {

            User user = entry.getKey();
            List<UserVocabProgress> list = entry.getValue();
            int count = list.size();

            if (count == 0) continue;

            // 🔔 1 NOTIFICATION / USER
            notificationService.create(
                    user,
                    "📚 Nhắc ôn từ vựng",
                    "Bạn có " + count + " từ chưa thuộc cần ôn hôm nay"
            );

            // ⏰ cập nhật lastReminderSentAt cho toàn bộ vocab
            LocalDateTime now = LocalDateTime.now();
            list.forEach(p -> p.setLastReminderSentAt(now));

            progressRepo.saveAll(list);

            System.out.println(
                    "🔔 Reminder sent | user=" + user.getId()
                            + " | count=" + count
            );
        }
    }
}
