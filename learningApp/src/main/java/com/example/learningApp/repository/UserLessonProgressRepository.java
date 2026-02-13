package com.example.learningApp.repository;

import com.example.learningApp.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserLessonProgressRepository
        extends JpaRepository<UserLessonProgress, String> {
    @Query("""
    SELECT AVG(
        CASE WHEN p.completed = true THEN 100.0 ELSE 0.0 END
    )
    FROM UserLessonProgress p
    WHERE p.user.id = :userId
    AND p.lesson.section.id = :sectionId
""")
    Double calculateSectionPercent(String userId, String sectionId);

    Optional<UserLessonProgress>
    findByUserIdAndLessonId(String userId, String lessonId);
}
