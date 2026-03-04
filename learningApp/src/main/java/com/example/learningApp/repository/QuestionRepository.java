package com.example.learningApp.repository;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findBySectionId(String sectionId);

    @Query("""
                SELECT q FROM Question q
                JOIN q.section s
                JOIN s.exams e
                WHERE q.id = :questionId
                AND e.id = :examId
            """)
    Optional<Question> findByIdAndExamId(
            @Param("questionId") String questionId,
            @Param("examId") String examId);

    @Query("""
                SELECT q FROM Question q
                LEFT JOIN FETCH q.passage p
                JOIN q.section s
                JOIN s.exams e
                WHERE e.id = :examId
                ORDER BY s.sectionOrder ASC, q.questionOrder ASC
            """)
    List<Question> findAllByExamId(@Param("examId") String examId);
}
