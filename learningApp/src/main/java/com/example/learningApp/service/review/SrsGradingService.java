package com.example.learningApp.service.review;

import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.ReviewGrade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SrsGradingService {

    private static final double MIN_EASE_FACTOR = 1.3;
    private static final double MAX_EASE_FACTOR = 3.0;

    public void applyGrade(UserVocabProgress progress, ReviewGrade grade, LocalDateTime now) {
        int currentInterval = Math.max(progress.getIntervalDays(), 0);
        double currentEase = clampEase(progress.getEaseFactor());

        progress.setLastReviewedAt(now);

        switch (grade) {
            case AGAIN -> {
                progress.setStatus(LearningStatus.FORGOTTEN);
                progress.setLapseCount(progress.getLapseCount() + 1);
                progress.setForgottenCount(progress.getForgottenCount() + 1);
                progress.setIntervalDays(1);
                progress.setEaseFactor(clampEase(currentEase - 0.2));
                progress.setNextReviewAt(now.plusHours(8));
            }
            case HARD -> {
                int nextInterval = nextHardInterval(currentInterval);
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(nextInterval);
                progress.setEaseFactor(clampEase(currentEase - 0.15));
                progress.setNextReviewAt(now.plusDays(nextInterval));
            }
            case GOOD -> {
                int nextInterval = nextGoodInterval(currentInterval, currentEase);
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(nextInterval);
                progress.setEaseFactor(clampEase(currentEase + 0.02));
                progress.setNextReviewAt(now.plusDays(nextInterval));
            }
            case EASY -> {
                int nextInterval = nextEasyInterval(currentInterval, currentEase);
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(nextInterval);
                progress.setEaseFactor(clampEase(currentEase + 0.15));
                progress.setNextReviewAt(now.plusDays(nextInterval));
            }
        }
    }

    private int nextHardInterval(int currentInterval) {
        if (currentInterval <= 1) return 3;
        return Math.max(currentInterval + 1, (int) Math.ceil(currentInterval * 1.2));
    }

    private int nextGoodInterval(int currentInterval, double easeFactor) {
        if (currentInterval <= 0) return 1;
        if (currentInterval == 1) return 3;
        if (currentInterval == 3) return 7;
        if (currentInterval == 7) return 14;
        if (currentInterval == 14) return 30;
        return Math.max(currentInterval + 1, (int) Math.round(currentInterval * easeFactor));
    }

    private int nextEasyInterval(int currentInterval, double easeFactor) {
        if (currentInterval <= 0) return 4;
        if (currentInterval <= 3) return 7;
        return Math.max(currentInterval + 2, (int) Math.round(currentInterval * easeFactor * 1.3));
    }

    private double clampEase(double value) {
        return Math.max(MIN_EASE_FACTOR, Math.min(MAX_EASE_FACTOR, value));
    }
}

