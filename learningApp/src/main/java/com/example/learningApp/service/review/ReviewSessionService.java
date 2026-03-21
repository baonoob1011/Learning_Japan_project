package com.example.learningApp.service.review;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.review.*;
import com.example.learningApp.entity.ReviewSession;
import com.example.learningApp.entity.ReviewSessionItem;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.NotificationType;
import com.example.learningApp.enums.ReviewQueueType;
import com.example.learningApp.enums.ReviewSessionStatus;
import com.example.learningApp.repository.ReviewSessionItemRepository;
import com.example.learningApp.repository.ReviewSessionRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewSessionService {

    private final EntityFinder finder;
    private final UserRepository userRepository;
    private final UserVocabProgressRepository progressRepository;
    private final ReviewSessionRepository sessionRepository;
    private final ReviewSessionItemRepository sessionItemRepository;
    private final TodayQueueBuilder queueBuilder;
    private final NotificationService notificationService;

    @Value("${app.srs.max-review-per-day:20}")
    private int maxReviewPerDay;

    @Value("${app.srs.max-new-per-day:10}")
    private int maxNewPerDay;

    @Value("${app.srs.max-overdue-per-day:5}")
    private int maxOverduePerDay;

    public TodayReviewQueueResponse getTodayQueueForCurrentUser() {
        User user = finder.userById();
        ReviewSession session = getOrCreateTodaySession(user, LocalDate.now());
        return toTodayQueueResponse(session);
    }

    public ReviewSession getOrCreateTodaySession(User user, LocalDate date) {
        return sessionRepository.findByUserAndDate(user, date)
                .orElseGet(() -> {
                    ReviewSession created = buildSession(user, date);
                    notifySessionReady(user, created);
                    return created;
                });
    }

    public PageResponse<ReviewSessionResponse> getMySessionHistory(int page, int size) {
        User user = finder.userById();
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewSessionResponse> result = sessionRepository.findByUserOrderByDateDesc(user, pageable)
                .map(this::toSessionResponse);

        return PageResponse.<ReviewSessionResponse>builder()
                .data(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public Optional<ReviewSession> findTodaySession(User user) {
        return sessionRepository.findByUserAndDate(user, LocalDate.now());
    }

    public void markItemCompletedIfInTodaySession(User user, String wordProgressId) {
        sessionItemRepository
                .findBySession_UserAndSession_DateAndWordProgress_Id(user, LocalDate.now(), wordProgressId)
                .ifPresent(item -> {
                    if (!item.isCompleted()) {
                        item.setCompleted(true);
                        item.setCompletedAt(LocalDateTime.now());
                        sessionItemRepository.save(item);
                    }

                    ReviewSession session = item.getSession();
                    long remaining = sessionItemRepository.countBySession_IdAndCompletedFalse(session.getId());
                    if (remaining == 0 && session.getStatus() != ReviewSessionStatus.COMPLETED) {
                        session.setStatus(ReviewSessionStatus.COMPLETED);
                        sessionRepository.save(session);
                    } else if (remaining > 0 && session.getStatus() == ReviewSessionStatus.PENDING) {
                        session.setStatus(ReviewSessionStatus.IN_PROGRESS);
                        sessionRepository.save(session);
                    }
                });
    }

    public List<ReviewWordItemResponse> getOverdueWordsForCurrentUser(int limit) {
        User user = finder.userById();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return progressRepository.findByUser(user).stream()
                .filter(p -> p.getStatus() != LearningStatus.NEW)
                .filter(p -> p.getNextReviewAt() != null && p.getNextReviewAt().isBefore(todayStart))
                .sorted(Comparator
                        .comparing(UserVocabProgress::getNextReviewAt)
                        .thenComparing(UserVocabProgress::getLapseCount, Comparator.reverseOrder()))
                .limit(limit)
                .map(p -> toQueueItemResponse(p, ReviewQueueType.OVERDUE, false))
                .toList();
    }

    public Map<String, Integer> getWordStatsForCurrentUser() {
        User user = finder.userById();
        ensureProgressRows(user);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDate.now().atStartOfDay();

        int newCount = 0;
        int dueCount = 0;
        int overdueCount = 0;

        for (UserVocabProgress p : progressRepository.findByUser(user)) {
            if (p.getStatus() == LearningStatus.NEW) {
                newCount++;
                continue;
            }
            if (p.getNextReviewAt() == null || p.getNextReviewAt().isAfter(now)) {
                continue;
            }
            if (p.getNextReviewAt().isBefore(start))
                overdueCount++;
            else
                dueCount++;
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("newCount", newCount);
        stats.put("dueCount", dueCount);
        stats.put("overdueCount", overdueCount);
        stats.put("total", newCount + dueCount + overdueCount);
        return stats;
    }

    public ReviewSession createTodaySessionForUser(String userId, LocalDate date) {
        User user = finder.userId(userId);
        return getOrCreateTodaySession(user, date);
    }

    public void createTodaySessionsForAllUsers(LocalDate date) {
        for (User user : userRepository.findAll()) {
            getOrCreateTodaySession(user, date);
        }
    }

    private ReviewSession buildSession(User user, LocalDate date) {
        ensureProgressRows(user);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<UserVocabProgress> all = progressRepository.findByUser(user);
        List<UserVocabProgress> newWords = all.stream()
                .filter(p -> p.getStatus() == LearningStatus.NEW)
                .toList();

        List<UserVocabProgress> dueToday = all.stream()
                .filter(p -> p.getStatus() != LearningStatus.NEW)
                .filter(p -> p.getNextReviewAt() != null)
                .filter(p -> !p.getNextReviewAt().isBefore(start) && p.getNextReviewAt().isBefore(end))
                .toList();

        List<UserVocabProgress> overdue = all.stream()
                .filter(p -> p.getStatus() != LearningStatus.NEW)
                .filter(p -> p.getNextReviewAt() != null && p.getNextReviewAt().isBefore(start))
                .peek(p -> p.setStatus(LearningStatus.OVERDUE))
                .toList();

        List<TodayQueueBuilder.QueueEntry> queueEntries = queueBuilder.buildTodayQueue(
                dueToday,
                overdue,
                newWords,
                maxReviewPerDay,
                maxNewPerDay,
                maxOverduePerDay);

        ReviewSession session = ReviewSession.builder()
                .user(user)
                .date(date)
                .status(queueEntries.isEmpty() ? ReviewSessionStatus.COMPLETED : ReviewSessionStatus.PENDING)
                .dueCount((int) queueEntries.stream().filter(e -> e.type() == ReviewQueueType.DUE_TODAY).count())
                .overdueInjectedCount(
                        (int) queueEntries.stream().filter(e -> e.type() == ReviewQueueType.OVERDUE).count())
                .newCount((int) queueEntries.stream().filter(e -> e.type() == ReviewQueueType.NEW).count())
                .totalCount(queueEntries.size())
                .build();

        for (int i = 0; i < queueEntries.size(); i++) {
            TodayQueueBuilder.QueueEntry entry = queueEntries.get(i);
            ReviewSessionItem item = ReviewSessionItem.builder()
                    .session(session)
                    .wordProgress(entry.progress())
                    .queueType(entry.type())
                    .orderIndex(i)
                    .completed(false)
                    .build();
            session.getItems().add(item);
        }

        return sessionRepository.save(session);
    }

    private void notifySessionReady(User user, ReviewSession session) {
        if (session.getTotalCount() <= 0) {
            return;
        }
        String content = "Hôm nay bạn có " + session.getDueCount() + " từ đến hạn ôn";
        if (session.getOverdueInjectedCount() > 0) {
            content += " và " + session.getOverdueInjectedCount() + " từ quá hạn đã được chọn để ôn dần.";
        } else {
            content += ".";
        }
        String metadata = "{sessionId:" + session.getId()
                + ",dueCount:" + session.getDueCount()
                + ",overdueInjectedCount:" + session.getOverdueInjectedCount()
                + ",newCount:" + session.getNewCount()
                + "}";
        notificationService.create(
                user,
                "Phiên ôn hôm nay đã sẵn sàng",
                content,
                NotificationType.REVIEW_REMINDER,
                metadata);
    }

    private void ensureProgressRows(User user) {
        Set<String> existingVocabIds = progressRepository.findByUser(user).stream()
                .map(p -> p.getVocab().getId())
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        List<UserVocabProgress> toCreate = new ArrayList<>();
        for (Vocab vocab : user.getSavedVocabs()) {
            if (existingVocabIds.contains(vocab.getId())) {
                continue;
            }
            UserVocabProgress p = UserVocabProgress.builder()
                    .user(user)
                    .vocab(vocab)
                    .status(LearningStatus.NEW)
                    .reviewCount(0)
                    .forgottenCount(0)
                    .intervalDays(0)
                    .easeFactor(2.5)
                    .lapseCount(0)
                    .successCount(0)
                    .nextReviewAt(LocalDateTime.now())
                    .build();
            toCreate.add(p);
        }

        if (!toCreate.isEmpty()) {
            progressRepository.saveAll(toCreate);
        }
    }

    private TodayReviewQueueResponse toTodayQueueResponse(ReviewSession session) {
        List<ReviewWordItemResponse> queue = new ArrayList<>();
        boolean changed = false;

        for (ReviewSessionItem item : sessionItemRepository.findBySession_IdOrderByOrderIndexAsc(session.getId())) {
            String progressId = item.getWordProgress() == null ? null : item.getWordProgress().getId();
            if (progressId == null) {
                sessionItemRepository.delete(item);
                changed = true;
                continue;
            }

            Optional<UserVocabProgress> progressOpt = progressRepository.findById(progressId);
            if (progressOpt.isEmpty()) {
                sessionItemRepository.delete(item);
                changed = true;
                continue;
            }

            queue.add(toQueueItemResponse(progressOpt.get(), item.getQueueType(), item.isCompleted()));
        }

        if (changed) {
            refreshSessionCounters(session, queue);
        }

        long totalOverduePool = progressRepository.findByUser(session.getUser()).stream()
                .filter(p -> p.getStatus() != LearningStatus.NEW)
                .filter(p -> p.getNextReviewAt() != null)
                .filter(p -> p.getNextReviewAt().isBefore(session.getDate().atStartOfDay()))
                .count();

        String recoveryMessage = null;
        if (totalOverduePool > session.getOverdueInjectedCount()) {
            recoveryMessage = "Bạn đang có nhiều từ chờ ôn, hôm nay mình chọn ra những từ ưu tiên nhất để bạn bắt nhịp lại.";
        }

        TodayReviewSummaryResponse summary = TodayReviewSummaryResponse.builder()
                .newCount(session.getNewCount())
                .dueCount(session.getDueCount())
                .overdueCount((int) totalOverduePool)
                .todayQueueCount(session.getTotalCount())
                .build();

        return TodayReviewQueueResponse.builder()
                .sessionId(session.getId())
                .summary(summary)
                .todayQueue(queue)
                .recoveryMessage(recoveryMessage)
                .build();
    }

    private void refreshSessionCounters(ReviewSession session, List<ReviewWordItemResponse> queue) {
        session.setDueCount((int) queue.stream().filter(q -> q.getType() == ReviewQueueType.DUE_TODAY).count());
        session.setOverdueInjectedCount(
                (int) queue.stream().filter(q -> q.getType() == ReviewQueueType.OVERDUE).count());
        session.setNewCount((int) queue.stream().filter(q -> q.getType() == ReviewQueueType.NEW).count());
        session.setTotalCount(queue.size());
        if (queue.isEmpty()) {
            session.setStatus(ReviewSessionStatus.COMPLETED);
        }
        sessionRepository.save(session);
    }

    private ReviewWordItemResponse toQueueItemResponse(UserVocabProgress progress, ReviewQueueType type,
            boolean completed) {
        return ReviewWordItemResponse.builder()
                .wordProgressId(progress.getId())
                .vocabId(progress.getVocab().getId())
                .word(progress.getVocab().getSurface())
                .meaning(progress.getVocab().getTranslated())
                .type(type)
                .status(progress.getStatus())
                .lapseCount(progress.getLapseCount())
                .intervalDays(progress.getIntervalDays())
                .nextReviewAt(progress.getNextReviewAt())
                .completed(completed)
                .build();
    }

    private ReviewSessionResponse toSessionResponse(ReviewSession session) {
        return ReviewSessionResponse.builder()
                .id(session.getId())
                .date(session.getDate())
                .status(session.getStatus())
                .dueCount(session.getDueCount())
                .overdueInjectedCount(session.getOverdueInjectedCount())
                .newCount(session.getNewCount())
                .totalCount(session.getTotalCount())
                .build();
    }
}
