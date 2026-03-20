package com.example.learningApp.repository;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.UserAnswer;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.enums.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, String> {

    @Query("""
                SELECT COUNT(ua)
                FROM UserAnswer ua
                WHERE ua.user = :user
                AND ua.question.questionType IN :types
            """)
    long countTotalByUserAndTypes(User user, List<AssessmentType> types);

    @Query("""
                SELECT COUNT(ua)
                FROM UserAnswer ua
                WHERE ua.user = :user
                AND ua.question.questionType IN :types
                AND ua.correct = true
            """)
    long countCorrectByUserAndTypes(User user, List<AssessmentType> types);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserAnswer ua WHERE ua.question.id IN (SELECT q.id FROM Question q JOIN q.exams e WHERE e.id = :examId)")
    void deleteByExamId(String examId);
}
