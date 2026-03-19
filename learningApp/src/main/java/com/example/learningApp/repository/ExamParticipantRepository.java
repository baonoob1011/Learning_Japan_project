package com.example.learningApp.repository;

import com.example.learningApp.entity.ExamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamParticipantRepository extends JpaRepository<ExamParticipant, String> {

    List<ExamParticipant> findByCompletedFalse();

    long countByUser_IdAndExam_IdAndStartedAtBetween(
            String userId,
            String examId,
            LocalDateTime start,
            LocalDateTime end);

    long countByUser_IdAndStartedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

    void deleteByExam_Id(String examId);
}
