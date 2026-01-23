package com.example.learningApp.repository;

import com.example.learningApp.entity.UserVocabProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVocabProgressRepository extends JpaRepository<UserVocabProgress, Long> {

    Optional<UserVocabProgress> findByUser_IdAndVocab_Id(String userId, String vocabId);

    List<UserVocabProgress> findByUser_IdAndNextReviewAtBefore(
            String userId,
            LocalDateTime time
    );

    List<UserVocabProgress> findByUser_Id(String userId);
}
