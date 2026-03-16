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

        boolean existsBySectionAndQuestionOrderAndQuestionType(com.example.learningApp.entity.ExamSection section,
                        Integer questionOrder, com.example.learningApp.enums.AssessmentType questionType);

        boolean existsBySectionAndQuestionText(com.example.learningApp.entity.ExamSection section, String questionText);

        boolean existsBySectionAndQuestionTextAndAnswer(com.example.learningApp.entity.ExamSection section,
                        String questionText, String answer);

        java.util.Optional<Question> findBySectionAndQuestionTextAndAnswer(
                        com.example.learningApp.entity.ExamSection section,
                        String questionText, String answer);

        @Query("""
                            SELECT q FROM Question q
                            JOIN q.exams e
                            WHERE q.id = :questionId
                            AND e.id = :examId
                        """)
        Optional<Question> findByIdAndExamId(
                        @Param("questionId") String questionId,
                        @Param("examId") String examId);

        @Query("""
                            SELECT q FROM Question q
                            LEFT JOIN FETCH q.passage p
                            JOIN q.exams e
                            WHERE e.id = :examId
                            ORDER BY q.section.sectionOrder ASC, q.questionOrder ASC
                        """)
        List<Question> findAllByExamId(@Param("examId") String examId);

        @Query("""
                            SELECT COUNT(q) FROM Question q
                            JOIN q.exams e
                            WHERE e.id = :examId
                        """)
        long countAllByExamId(@Param("examId") String examId);
}
