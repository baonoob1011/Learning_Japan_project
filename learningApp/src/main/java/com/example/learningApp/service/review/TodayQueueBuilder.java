package com.example.learningApp.service.review;

import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.ReviewQueueType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TodayQueueBuilder {

    public record QueueEntry(UserVocabProgress progress, ReviewQueueType type) {
    }

    public List<QueueEntry> buildTodayQueue(
            List<UserVocabProgress> dueTodayWords,
            List<UserVocabProgress> overdueWords,
            List<UserVocabProgress> newWords,
            int maxReviewPerDay,
            int maxNewPerDay,
            int maxOverduePerDay
    ) {
        List<UserVocabProgress> dueSorted = dueTodayWords.stream()
                .sorted(Comparator.comparing(UserVocabProgress::getNextReviewAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<UserVocabProgress> overdueSorted = overdueWords.stream()
                .sorted(Comparator
                        .comparing(UserVocabProgress::getNextReviewAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(UserVocabProgress::getLapseCount, Comparator.reverseOrder()))
                .toList();

        List<UserVocabProgress> newSorted = newWords.stream()
                .sorted(Comparator.comparing(UserVocabProgress::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<QueueEntry> result = new ArrayList<>();

        int dueTake = Math.min(dueSorted.size(), maxReviewPerDay);
        for (int i = 0; i < dueTake; i++) {
            result.add(new QueueEntry(dueSorted.get(i), ReviewQueueType.DUE_TODAY));
        }

        int remainingForReview = maxReviewPerDay - result.size();
        int overdueTake = Math.min(Math.min(overdueSorted.size(), maxOverduePerDay), remainingForReview);
        for (int i = 0; i < overdueTake; i++) {
            result.add(new QueueEntry(overdueSorted.get(i), ReviewQueueType.OVERDUE));
        }

        int remainingTotal = maxReviewPerDay - result.size();
        int newTake = Math.min(Math.min(newSorted.size(), maxNewPerDay), remainingTotal);
        for (int i = 0; i < newTake; i++) {
            result.add(new QueueEntry(newSorted.get(i), ReviewQueueType.NEW));
        }

        return result;
    }
}

