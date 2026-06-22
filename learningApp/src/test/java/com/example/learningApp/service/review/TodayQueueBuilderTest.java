package com.example.learningApp.service.review;

import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.ReviewQueueType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TodayQueueBuilderTest {

    private final TodayQueueBuilder builder = new TodayQueueBuilder();

    @Test
    void buildTodayQueue_shouldPrioritizeDueThenOverdueThenNewWithLimits() {
        List<UserVocabProgress> due = List.of(progress(1), progress(2), progress(3), progress(4), progress(5), progress(6), progress(7), progress(8));
        List<UserVocabProgress> overdue = List.of(progress(-1), progress(-2), progress(-3), progress(-4), progress(-5), progress(-6));
        List<UserVocabProgress> newWords = List.of(progress(0), progress(0), progress(0), progress(0), progress(0));

        List<TodayQueueBuilder.QueueEntry> queue = builder.buildTodayQueue(due, overdue, newWords, 15, 5, 5);

        assertEquals(15, queue.size());
        assertEquals(8, queue.stream().filter(q -> q.type() == ReviewQueueType.DUE_TODAY).count());
        assertEquals(5, queue.stream().filter(q -> q.type() == ReviewQueueType.OVERDUE).count());
        assertEquals(2, queue.stream().filter(q -> q.type() == ReviewQueueType.NEW).count());
    }

    private UserVocabProgress progress(int dayOffset) {
        return UserVocabProgress.builder()
                .nextReviewAt(LocalDateTime.now().plusDays(dayOffset))
                .createdAt(LocalDateTime.now().minusDays(5))
                .lapseCount(Math.max(0, -dayOffset))
                .build();
    }
}
