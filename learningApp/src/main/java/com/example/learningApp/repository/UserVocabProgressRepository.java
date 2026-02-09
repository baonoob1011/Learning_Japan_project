package com.example.learningApp.repository;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVocabProgressRepository extends JpaRepository<UserVocabProgress, Long> {

    Optional<UserVocabProgress> findByUserAndVocab(User user, Vocab vocab);

    List<UserVocabProgress> findByStatusIn(List<LearningStatus> statuses);

    // ===== dùng khi user bấm nút =====
    Optional<UserVocabProgress> findByUserAndVocab_Id(
            User user,
            String vocabId
    );

    List<UserVocabProgress> findByStatusAndLastReviewedAtLessThanEqual(
            LearningStatus status,
            LocalDateTime time
    );


}


