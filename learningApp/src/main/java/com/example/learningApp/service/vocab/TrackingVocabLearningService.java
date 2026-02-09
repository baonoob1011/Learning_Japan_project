package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.repository.UserVocabProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrackingVocabLearningService {

    private final UserVocabProgressRepository progressRepo;
    private final EntityFinder finder;

    public void markVocab(MarkVocabRequest request) {

        User user = finder.userById();
        Vocab vocab = finder.vocabId(request.getVocabId());

        UserVocabProgress progress = progressRepo
                .findByUserAndVocab_Id(user, vocab.getId())
                .orElseGet(() -> createNewProgress(user, vocab));

        LocalDateTime now = LocalDateTime.now();
        progress.setLastReviewedAt(now);

        if (request.isRemembered()) {
            handleRemembered(progress);
        } else {
            handleForgotten(progress);
        }

        progressRepo.save(progress);
    }

    // ================= PRIVATE =================

    private void handleRemembered(UserVocabProgress p) {

        p.setReviewCount(p.getReviewCount() + 1);

        if (p.getReviewCount() >= 3) {
            p.setStatus(LearningStatus.KNOWN);
        } else {
            p.setStatus(LearningStatus.LEARNING);
        }
    }

    private void handleForgotten(UserVocabProgress p) {

        p.setForgottenCount(p.getForgottenCount() + 1);
        p.setStatus(LearningStatus.FORGOTTEN);
    }

    private UserVocabProgress createNewProgress(User user, Vocab vocab) {
        return UserVocabProgress.builder()
                .user(user)
                .vocab(vocab)
                .status(LearningStatus.NEW)
                .reviewCount(0)
                .forgottenCount(0)
                .lastReviewedAt(LocalDateTime.now())
                .build();
    }
}
