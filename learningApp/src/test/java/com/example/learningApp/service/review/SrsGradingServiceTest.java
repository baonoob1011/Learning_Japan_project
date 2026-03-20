package com.example.learningApp.service.review;

import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.ReviewGrade;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SrsGradingServiceTest {

    private final SrsGradingService service = new SrsGradingService();

    @Test
    void again_shouldMoveToRelearningAndSetSoonReview() {
        UserVocabProgress p = UserVocabProgress.builder()
                .status(LearningStatus.REVIEW)
                .intervalDays(7)
                .easeFactor(2.5)
                .successCount(4)
                .build();

        LocalDateTime now = LocalDateTime.now();
        service.applyGrade(p, ReviewGrade.AGAIN, now);

        assertEquals(LearningStatus.RELEARNING, p.getStatus());
        assertEquals(1, p.getIntervalDays());
        assertTrue(p.getNextReviewAt().isAfter(now.plusHours(7)));
        assertEquals(1, p.getLapseCount());
    }

    @Test
    void good_shouldIncreaseIntervalAndKeepSrsActive() {
        UserVocabProgress p = UserVocabProgress.builder()
                .status(LearningStatus.LEARNING)
                .intervalDays(3)
                .easeFactor(2.5)
                .successCount(1)
                .build();

        LocalDateTime now = LocalDateTime.now();
        service.applyGrade(p, ReviewGrade.GOOD, now);

        assertEquals(7, p.getIntervalDays());
        assertTrue(p.getNextReviewAt().isAfter(now.plusDays(6)));
        assertEquals(LearningStatus.REVIEW, p.getStatus());
        assertEquals(2, p.getSuccessCount());
    }
}
