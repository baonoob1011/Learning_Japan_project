package com.example.learningApp.repository;

import com.example.learningApp.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamAnswerRepository
        extends JpaRepository<ExamAnswer, String> {

    Optional<ExamAnswer> findByParticipant_IdAndQuestion_Id(
            String participantId,
            String questionId
    );
    @Query("""
        SELECT q.id, a.answer
        FROM ExamAnswer a
        JOIN a.question q
        WHERE a.participant.id = :participantId
    """)
    List<Object[]> findAnswersByParticipantId(String participantId);
}
