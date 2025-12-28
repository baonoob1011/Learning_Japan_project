package com.example.learningApp.service.flashcard;

import com.example.learningApp.entity.*;
import com.example.learningApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFlashcardService {
    private final UserFlashcardRepository userFlashcardRepository;
    private final VocabularyRepository vocabularyRepository;
    private final YoutubeVideoRepository videoRepository;
    private final UserRepository userRepository;
    private final SpeakingLogRepository speakingLogRepository;

    @Transactional
    public UserFlashcard createFlashcard(String userId, String vocabularyId, String sourceSentence, String videoId) {
        if (userFlashcardRepository.existsByUserIdAndVocabulary_Id(userId, vocabularyId)) {
            throw new RuntimeException("Flashcard already exists");
        }

        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId).orElseThrow(
                () -> new RuntimeException("Vocabulary not found")
        );

        YoutubeVideo video = videoRepository.findById(videoId).orElseThrow(
                () -> new RuntimeException("Video not found")
        );

        UserFlashcard userFlashcard = UserFlashcard.builder()
                .id(userId)
                .vocabulary(vocabulary)
                .sourceSentence(sourceSentence)
                .video(video)
                .level(0)
                .streak(0)
                .nextReviewAt(LocalDateTime.now())
                .build();

        return userFlashcardRepository.save(userFlashcard);
    }

    public List<UserFlashcard> getCardsDueForReview(String userId) {
        return userFlashcardRepository.findByUserIdAndNextReviewAtBefore(userId, LocalDateTime.now());
    }

    @Transactional
    public void reviewCard(String cardId, String rating){
        UserFlashcard card = userFlashcardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReview;

        switch (rating) {
            case "AGAIN":
                card.setLevel(0);
                card.setStreak(0);
                nextReview = now.plusMinutes(10);
                break;
            case "HARD":
                nextReview = now.plusHours(12);
                break;
            case "GOOD":
                card.setLevel(card.getLevel() + 1);
                card.setStreak(card.getStreak() + 1);
                // Tính thời gian dựa trên level (ví dụ: 1 ngày, 3 ngày, 7 ngày...)
                nextReview = calculateNextReviewTime(card.getLevel());
                break;
            case "EASY": // Quá dễ -> Nhảy cóc level
                card.setLevel(card.getLevel() + 2);
                card.setStreak(card.getStreak() + 2);
                nextReview = calculateNextReviewTime(card.getLevel());
                break;

            default:
                throw new IllegalArgumentException("Rating không hợp lệ");
        }
        card.setNextReviewAt(nextReview);
        card.setLastReviewedAt(now);

        userFlashcardRepository.save(card);

    }

    @Transactional
    public void submitSpeakingResult(String userId, Integer durationSeconds){

        SpeakingLog speakingLog = SpeakingLog.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .duration(durationSeconds)
                .build();

        speakingLogRepository.save(speakingLog);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        long currentTotal = user.getTotalSpeakingSeconds() == null ? 0 : user.getTotalSpeakingSeconds();
        user.setTotalSpeakingSeconds(currentTotal + durationSeconds);

        userRepository.save(user);
    }

    // Hàm phụ trợ tính thời gian
    private LocalDateTime calculateNextReviewTime(int level) {
        int daysToAdd;
        switch (level) {
            case 1: daysToAdd = 1; break;
            case 2: daysToAdd = 3; break;
            case 3: daysToAdd = 7; break;
            case 4: daysToAdd = 14; break;
            case 5: daysToAdd = 30; break;
            default: daysToAdd = 30 + (level - 5) * 20;
        }
        return LocalDateTime.now().plusDays(daysToAdd);
    }
}
