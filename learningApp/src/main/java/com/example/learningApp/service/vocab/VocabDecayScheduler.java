package com.example.learningApp.service.vocab;

import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.repository.UserVocabProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VocabDecayScheduler {

    private final UserVocabProgressRepository progressRepo;

    // test: 10s | prod: 1 ngày 1 lần (vd: 2h sáng)
    @Scheduled(fixedDelay = 10 * 1000)
    @Transactional
    public void decayLearningToForgotten() {

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        List<UserVocabProgress> expiredLearningVocabs =
                progressRepo.findByStatusAndLastReviewedAtLessThanEqual(
                        LearningStatus.LEARNING,
                        threeDaysAgo
                );

        if (expiredLearningVocabs.isEmpty()) return;

        expiredLearningVocabs.forEach(p ->
                p.setStatus(LearningStatus.FORGOTTEN)
        );

        progressRepo.saveAll(expiredLearningVocabs);

        System.out.println(
                "⏳ Decay job | LEARNING → FORGOTTEN | count="
                        + expiredLearningVocabs.size()
        );
    }
}
