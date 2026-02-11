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

        // test: 10s | prod: 1 lần / ngày
        @Scheduled(fixedDelay = 10 * 1000)
        @Transactional
        public void remindForgottenVocabs() {

            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

            List<LearningStatus> NEED_REVIEW =
                    List.of(LearningStatus.FORGOTTEN, LearningStatus.LEARNING);

            // 🔥 LẤY TOÀN BỘ vocab chưa thuộc
            List<UserVocabProgress> dueList =
                    progressRepo.findByStatusIn(NEED_REVIEW);

            // 🔥 chống spam + group theo user
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

                notificationService.create(
                        user,
                        "📚 Nhắc ôn từ vựng",
                        "Bạn có " + count + " từ chưa thuộc cần ôn hôm nay"
                );

                LocalDateTime now = LocalDateTime.now();
                list.forEach(p -> p.setLastReminderSentAt(now));
                progressRepo.saveAll(list);

                System.out.println(
                        "🔔 Daily reminder | user=" + user.getId()
                                + " | count=" + count
                );
            }
        }
    }
