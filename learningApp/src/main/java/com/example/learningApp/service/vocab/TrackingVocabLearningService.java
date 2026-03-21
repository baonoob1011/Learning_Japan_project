package com.example.learningApp.service.vocab;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.vocab.MarkVocabRequest;
import com.example.learningApp.dto.response.vocab.UserVocabProgressResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
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
                now
        );

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
                .listeningScore(0)
                .writingScore(0)
                .readingScore(0)
                .masteryLevel(0)
                .nextReviewAt(LocalDateTime.now())
                .lastReviewedAt(LocalDateTime.now())
                .build();
    }
}
