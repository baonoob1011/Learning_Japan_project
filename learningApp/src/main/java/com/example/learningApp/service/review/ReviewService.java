package com.example.learningApp.service.review;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.review.GradeReviewResponse;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.ReviewGrade;
import com.example.learningApp.repository.UserVocabProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final EntityFinder finder;
    private final UserVocabProgressRepository progressRepository;
    private final SrsGradingService gradingService;
    private final ReviewSessionService reviewSessionService;

    public GradeReviewResponse grade(String wordProgressId, ReviewGrade grade) {
        User user = finder.userById();
        UserVocabProgress progress = progressRepository.findById(wordProgressId)
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Word progress not found"));

        gradingService.applyGrade(progress, grade, LocalDateTime.now());
        progressRepository.save(progress);

        reviewSessionService.markItemCompletedIfInTodaySession(user, progress.getId());

        return GradeReviewResponse.builder()
                .wordProgressId(progress.getId())
                .status(progress.getStatus())
                .intervalDays(progress.getIntervalDays())
                .easeFactor(progress.getEaseFactor())
                .lapseCount(progress.getLapseCount())
                .successCount(progress.getSuccessCount())
                .nextReviewAt(progress.getNextReviewAt())
                .build();
    }

    public PageResponse<UserVocabProgressResponse> getHistory(int page, int size) {
        User user = finder.userById();
        Pageable pageable = PageRequest.of(page, size);

        Page<UserVocabProgressResponse> result = progressRepository.findByUserOrderByUpdatedAtDesc(user, pageable)
                .map(this::toResponse);

        return PageResponse.<UserVocabProgressResponse>builder()
                .data(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private UserVocabProgressResponse toResponse(UserVocabProgress p) {
        return UserVocabProgressResponse.builder()
                .vocabId(p.getVocab().getId())
                .vocabWord(p.getVocab().getSurface())
                .status(p.getStatus())
                .reviewCount(p.getReviewCount())
                .forgottenCount(p.getForgottenCount())
                .lastReviewedAt(p.getLastReviewedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

