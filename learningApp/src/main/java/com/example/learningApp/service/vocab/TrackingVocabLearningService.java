package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.dto.response.vocab.SmartFinalizeWordResponse;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.FlashcardStatus;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.ReviewGrade;
import com.example.learningApp.enums.StudyMode;
import com.example.learningApp.enums.Skill;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.review.SrsGradingService;
import com.example.learningApp.service.review.ReviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingVocabLearningService {

    private final UserVocabProgressRepository progressRepo;
    private final EntityFinder finder;
    private final SrsGradingService srsGradingService;
    private final UserRepository userRepository;
    private final ReviewSessionService reviewSessionService;

    public void markVocab(MarkVocabRequest request) {

        User user = finder.userById();
        Vocab vocab = finder.vocabId(request.getVocabId());

        // Nếu vocab chưa có trong danh sách đã lưu của user, ta auto thêm vào
        if (!user.getSavedVocabs().contains(vocab)) {
            user.getSavedVocabs().add(vocab);
            userRepository.save(user);
        }

        UserVocabProgress progress = progressRepo
                .findByUserAndVocab_Id(user, vocab.getId())
                .orElseGet(() -> createNewProgress(user, vocab));

        LocalDateTime now = LocalDateTime.now();

        // Gọi logic tính điểm đa phương thức
        srsGradingService.applyStudyModeResult(
                progress,
                request.getStudyMode(),
                request.isRemembered(),
                now);

        progress.setFlashcardStatus(request.isRemembered() ? FlashcardStatus.LEARNED : FlashcardStatus.NOT_LEARNED);

        progressRepo.save(progress);

        // Cập nhật session (xóa khỏi hàng đợi sau khi học xong kỹ năng)
        reviewSessionService.markItemCompletedIfInTodaySession(user, progress.getId());
    }

    public List<UserVocabProgressResponse> getMyLearningProgress() {

        User user = finder.userById();

        return progressRepo.findByUser(user)
                .stream()
                .map(p -> UserVocabProgressResponse.builder()
                        .vocabId(p.getVocab().getId())
                        .vocabWord(p.getVocab().getSurface())
                        .status(p.getStatus())
                        .reviewCount(p.getReviewCount())
                        .forgottenCount(p.getForgottenCount())
                        .listeningScore(p.getListeningScore())
                        .writingScore(p.getWritingScore())
                        .readingScore(p.getReadingScore())
                        .masteryLevel(p.getMasteryLevel())
                        .flashcardStatus(p.getFlashcardStatus())
                        .lastReviewedAt(p.getLastReviewedAt())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
    }

    public void recordSmartSkillAttempt(String vocabId, Skill skill, StudyMode studyMode, boolean success) {
        User user = finder.userById();
        Vocab vocab = finder.vocabId(vocabId);

        if (!user.getSavedVocabs().contains(vocab)) {
            user.getSavedVocabs().add(vocab);
            userRepository.save(user);
        }

        UserVocabProgress progress = progressRepo
                .findByUserAndVocab_Id(user, vocab.getId())
                .orElseGet(() -> createNewProgress(user, vocab));

        if (skill != null) {
            applySkillGranular(progress, skill, success);
        } else if (studyMode != null) {
            applySkillOnly(progress, studyMode, success);
        }

        progress.setLastReviewedAt(LocalDateTime.now());
        progressRepo.save(progress);
    }

    public SmartFinalizeWordResponse finalizeSmartWord(String vocabId, int wrongCount, boolean failedInRetry) {
        User user = finder.userById();
        Vocab vocab = finder.vocabId(vocabId);

        if (!user.getSavedVocabs().contains(vocab)) {
            user.getSavedVocabs().add(vocab);
            userRepository.save(user);
        }

        UserVocabProgress progress = progressRepo
                .findByUserAndVocab_Id(user, vocab.getId())
                .orElseGet(() -> createNewProgress(user, vocab));

        ReviewGrade grade = resolveGrade(wrongCount, failedInRetry);
        applySmartGrade(progress, grade, LocalDateTime.now());
        progressRepo.save(progress);
        reviewSessionService.markItemCompletedIfInTodaySession(user, progress.getId());

        return SmartFinalizeWordResponse.builder()
                .vocabId(vocabId)
                .wordProgressId(progress.getId())
                .grade(grade)
                .wrongCount(Math.max(0, wrongCount))
                .nextReviewAt(progress.getNextReviewAt())
                .build();
    }

    // ================= PRIVATE =================

    private UserVocabProgress createNewProgress(User user, Vocab vocab) {
        return UserVocabProgress.builder()
                .user(user)
                .vocab(vocab)
                .status(LearningStatus.NEW)
                .reviewCount(0)
                .forgottenCount(0)
                .intervalDays(0)
                .easeFactor(2.5)
                .lapseCount(0)
                .successCount(0)
                .listeningScore(0)
                .writingScore(0)
                .readingScore(0)
                .masteryLevel(0)
                .flashcardStatus(FlashcardStatus.NEW)
                .nextReviewAt(LocalDateTime.now())
                .lastReviewedAt(LocalDateTime.now())
                .build();
    }

    private ReviewGrade resolveGrade(int wrongCount, boolean failedInRetry) {
        if (failedInRetry) {
            return ReviewGrade.AGAIN;
        }

        int normalizedWrong = Math.max(0, wrongCount);
        if (normalizedWrong == 0) {
            return ReviewGrade.EASY;
        }
        if (normalizedWrong <= 2) {
            return ReviewGrade.GOOD;
        }
        return ReviewGrade.HARD;
    }

    private void applySkillGranular(UserVocabProgress progress, Skill skill, boolean success) {
        if (success) {
            switch (skill) {
                case LISTENING -> progress.setListeningScore(Math.min(100, progress.getListeningScore() + 15));
                case WRITING -> progress.setWritingScore(Math.min(100, progress.getWritingScore() + 20));
                case READING, TRANSLATION -> progress.setReadingScore(Math.min(100, progress.getReadingScore() + 10));
            }
        } else {
            switch (skill) {
                case LISTENING -> progress.setListeningScore(Math.max(0, progress.getListeningScore() - 5));
                case WRITING -> progress.setWritingScore(Math.max(0, progress.getWritingScore() - 10));
                case READING, TRANSLATION -> progress.setReadingScore(Math.max(0, progress.getReadingScore() - 5));
            }
        }
        updateMastery(progress);
    }

    private void applySkillOnly(UserVocabProgress progress, StudyMode mode, boolean success) {
        if (success) {
            switch (mode) {
                case LISTEN -> progress.setListeningScore(Math.min(100, progress.getListeningScore() + 15));
                case WRITE -> progress.setWritingScore(Math.min(100, progress.getWritingScore() + 20));
                case FLASHCARD, QUIZ -> progress.setReadingScore(Math.min(100, progress.getReadingScore() + 10));
            }
        } else {
            switch (mode) {
                case LISTEN -> progress.setListeningScore(Math.max(0, progress.getListeningScore() - 5));
                case WRITE -> progress.setWritingScore(Math.max(0, progress.getWritingScore() - 10));
                case FLASHCARD, QUIZ -> progress.setReadingScore(Math.max(0, progress.getReadingScore() - 5));
            }
        }
        updateMastery(progress);
    }

    private void updateMastery(UserVocabProgress progress) {
        int totalMastery = (progress.getListeningScore() + progress.getWritingScore() + progress.getReadingScore()) / 3;
        progress.setMasteryLevel(Math.min(100, totalMastery));
    }

    private void applySmartGrade(UserVocabProgress progress, ReviewGrade grade, LocalDateTime now) {
        progress.setLastReviewedAt(now);

        switch (grade) {
            case AGAIN -> {
                progress.setStatus(LearningStatus.FORGOTTEN);
                progress.setLapseCount(progress.getLapseCount() + 1);
                progress.setForgottenCount(progress.getForgottenCount() + 1);
                progress.setIntervalDays(0);
                progress.setNextReviewAt(now.plusHours(8));
            }
            case HARD -> {
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(1);
                progress.setNextReviewAt(now.plusDays(1));
            }
            case GOOD -> {
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(2);
                progress.setNextReviewAt(now.plusDays(2));
            }
            case EASY -> {
                progress.setStatus(LearningStatus.KNOWN);
                progress.setSuccessCount(progress.getSuccessCount() + 1);
                progress.setReviewCount(progress.getReviewCount() + 1);
                progress.setIntervalDays(3);
                progress.setNextReviewAt(now.plusDays(3));
            }
        }
    }
}
