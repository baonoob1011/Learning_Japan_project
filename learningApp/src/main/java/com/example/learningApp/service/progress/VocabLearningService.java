package com.example.learningApp.service.progress;


import com.example.learningApp.dto.request.vocab.StudyVocabRequest;
import com.example.learningApp.entity.*;
import com.example.learningApp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabLearningService {

    private final UserRepository userRepository;
    private final VocabRepository vocabRepository;
    private final UserVocabProgressRepository progressRepository;

    // 📌 User học / ôn vocab
    @Transactional
    public void studyVocab(StudyVocabRequest request) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userId=authentication.getName();


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vocab vocab = vocabRepository.findById(request.getVocabId())
                .orElseThrow(() -> new RuntimeException("Vocab not found"));

        UserVocabProgress progress = progressRepository
                .findByUser_IdAndVocab_Id(userId, request.getVocabId())
                .orElseGet(() -> UserVocabProgress.builder()
                        .user(user)
                        .vocab(vocab)
                        .reviewCount(0)
                        .mastered(request.isMastered())
                        .build()
                );

        // ===== Update progress =====
        progress.setReviewCount(progress.getReviewCount() + 1);
        progress.setNextReviewAt(calculateNextReview(progress.getReviewCount()));

        progressRepository.save(progress);
    }


    // 📌 Lấy vocab cần ôn hôm nay
    public List<UserVocabProgress> getTodayReview(String userId) {
        return progressRepository.findByUser_IdAndNextReviewAtBefore(
                userId,
                LocalDateTime.now()
        );
    }

    // 📌 Rule nhắc ôn (Spaced Repetition)
    private LocalDateTime calculateNextReview(int level) {
        return switch (level) {
            case 0 -> LocalDateTime.now().plusDays(1);
            case 1 -> LocalDateTime.now().plusDays(3);
            case 2 -> LocalDateTime.now().plusDays(7);
            case 3 -> LocalDateTime.now().plusDays(14);
            case 4 -> LocalDateTime.now().plusDays(30);
            default -> LocalDateTime.now().plusDays(60);
        };
    }
}
