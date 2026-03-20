package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import com.example.learningApp.enums.ReviewGrade;
import com.example.learningApp.repository.UserVocabProgressRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.review.SrsGradingService;
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
        ReviewGrade grade = request.isRemembered() ? ReviewGrade.GOOD : ReviewGrade.AGAIN;
        srsGradingService.applyGrade(progress, grade, now);

        progressRepo.save(progress);
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
                        .lastReviewedAt(p.getLastReviewedAt())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
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
                .nextReviewAt(LocalDateTime.now())
                .lastReviewedAt(LocalDateTime.now())
                .build();
    }
}

