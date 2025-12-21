package com.example.learningApp.repository;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findBySectionId(String sectionId);

    @Query("""
                SELECT q FROM Question q
                JOIN q.section s
                JOIN s.exam e
                WHERE q.id = :questionId
                AND e.id = :examId
            """)
    Question findByIdAndExamId(
            @Param("questionId") String questionId,
            @Param("examId") String examId
    );
}
