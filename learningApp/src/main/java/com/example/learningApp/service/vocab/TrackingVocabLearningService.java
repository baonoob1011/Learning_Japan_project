package com.example.learningApp.service.vocab;


import com.example.learningApp.common.CurrentUserService;
import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.entity.*;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.repository.UserRepository;
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
        var user=finder.userById();
        var vocab=finder.vocabId(request.getVocabId());
        UserVocabProgress progress =
                progressRepo.findByUserAndVocab(user, vocab)
                        .orElseGet(() -> createNewProgress(user, vocab));

        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setReviewCount(progress.getReviewCount() + 1);

        if (request.isRemembered()) {
            if (progress.getReviewCount() >= 3) {
                progress.setStatus(LearningStatus.MASTERED);
                progress.setNextReminderAt(null);
            } else {
                progress.setStatus(LearningStatus.LEARNING);
                progress.setNextReminderAt(nextReminder(progress.getReviewCount()));
            }
        } else {
            progress.setStatus(LearningStatus.LEARNING);
            progress.setNextReminderAt(nextReminder(progress.getReviewCount()));
        }

        progressRepo.save(progress);
    }

    private UserVocabProgress createNewProgress(User user, Vocab vocab) {
        UserVocabProgress p = new UserVocabProgress();
        p.setUser(user);
        p.setVocab(vocab);
        return p;
    }

    private LocalDateTime nextReminder(int count) {
        return switch (count) {
            case 1 -> LocalDateTime.now().plusHours(6);
            case 2 -> LocalDateTime.now().plusDays(1);
            case 3 -> LocalDateTime.now().plusDays(3);
            default -> LocalDateTime.now().plusDays(7);
        };
    }
}
