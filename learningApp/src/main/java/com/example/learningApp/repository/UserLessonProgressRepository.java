package com.example.learningApp.repository;

import com.example.learningApp.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserLessonProgressRepository
        extends JpaRepository<UserLessonProgress, String> {
    @Query("""
    SELECT 
        (SUM(COALESCE(ulp.progressPercent, 0)) / COUNT(l))
    FROM Lesson l
    LEFT JOIN UserLessonProgress ulp
        ON ulp.lesson.id = l.id
        AND ulp.user.id = :userId
    WHERE l.section.id = :sectionId
""")
    Double calculateSectionPercent(String userId, String sectionId);

    Optional<UserLessonProgress>
    findByUserIdAndLessonId(String userId, String lessonId);
}

