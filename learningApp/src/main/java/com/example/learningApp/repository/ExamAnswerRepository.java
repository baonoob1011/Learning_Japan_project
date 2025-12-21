package com.example.learningApp.repository;

import com.example.learningApp.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamAnswerRepository
        extends JpaRepository<ExamAnswer, String> {
    List<ExamAnswer> findByParticipant_Id(String participantId);

    Optional<ExamAnswer> findByParticipant_IdAndQuestionId(String participantId, String questionId);

}
