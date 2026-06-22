package com.example.learningApp.controller;

import com.example.learningApp.entity.User;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.service.notification.NotificationService;
import com.example.learningApp.service.review.ReviewSessionService;
import com.example.learningApp.service.vocab.IntradayVocabReminderJob;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class TestNotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ReviewSessionService reviewSessionService;
    private final UserVocabProgressRepository progressRepo;
    private final IntradayVocabReminderJob intradayJob;

    @GetMapping("/test-noti/{userId}")
    public String test(
            @PathVariable String userId,
            @RequestParam(defaultValue = "Nhac on tu vung theo SRS") String title,
            @RequestParam(defaultValue = "Hom nay ban co mot phien on moi.") String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        notificationService.create(user, title, content);
        return "OK";
    }

    @GetMapping("/trigger-srs-reminder/{userId}")
    public String triggerSrs(@PathVariable String userId) {
        reviewSessionService.createTodaySessionForUser(userId, LocalDate.now());
        return "SRS session created for user: " + userId;
    }

    @GetMapping("/make-due-all/{userId}")
    public String makeDueAll(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        var list = progressRepo.findByUser(user);
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3).minusMinutes(5);

        list.forEach(p -> {
            p.setNextReviewAt(threeDaysAgo);
            p.setLastReminderSentAt(threeDaysAgo);
        });

        progressRepo.saveAll(list);
        return "SUCCESS: All " + list.size() + " vocabs for user " + userId + " are now marked as overdue.";
    }

    @GetMapping("/trigger-intraday")
    public String triggerIntradayJob() {
        intradayJob.sendIntradayReminders();
        return "Da chay Job nhac nho giua ngay thanh cong. Hay kiem tra chuong thong bao!";
    }
}
