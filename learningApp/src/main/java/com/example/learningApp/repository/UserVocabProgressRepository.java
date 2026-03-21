package com.example.learningApp.repository;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserVocabProgress;
import com.example.learningApp.entity.Vocab;
import com.example.learningApp.enums.LearningStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVocabProgressRepository extends JpaRepository<UserVocabProgress, String> {

    Optional<UserVocabProgress> findByUserAndVocab(User user, Vocab vocab);

    Optional<UserVocabProgress> findByUserAndVocab_Id(User user, String vocabId);

    List<UserVocabProgress> findByUser(User user);

    Page<UserVocabProgress> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    List<UserVocabProgress> findByUserAndNextReviewAtLessThanEqual(User user, LocalDateTime time);

    List<UserVocabProgress> findByStatusIn(List<LearningStatus> statuses);

    List<UserVocabProgress> findByStatusAndLastReviewedAtLessThanEqual(
            LearningStatus status,
            LocalDateTime time);

    List<UserVocabProgress> findAllByNextReviewAtBetween(LocalDateTime start, LocalDateTime end);

    List<UserVocabProgress> findAllByNextReviewAtBefore(LocalDateTime time);

    void deleteByUserAndVocab(User user, Vocab vocab);
}
