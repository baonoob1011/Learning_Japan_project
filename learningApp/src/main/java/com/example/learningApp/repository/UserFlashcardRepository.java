package com.example.learningApp.repository;

import com.example.learningApp.entity.UserFlashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFlashcardRepository extends JpaRepository<UserFlashcard, String> {
    List<UserFlashcard> findByUserIdAndNextReviewAtBefore(String userId, LocalDateTime now);

    boolean existsByUserIdAndVocabulary_Id(String userId, String vocabularyId);
}
