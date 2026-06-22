package com.example.learningApp.service.vocab;

import com.example.learningApp.service.review.ReviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class VocabReminderScheduler {

    private final ReviewSessionService reviewSessionService;

    @Scheduled(cron = "${app.vocab-reminder.cron:0 0 7 * * *}")
    @Transactional
    public void buildDailyReviewSessions() {
        reviewSessionService.createTodaySessionsForAllUsers(LocalDate.now());
    }
}

