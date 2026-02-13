package com.example.learningApp.repository;

import com.example.learningApp.entity.UserSectionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSectionProgressRepository
        extends JpaRepository<UserSectionProgress, String> {
    @Query("""
    SELECT AVG(
        CASE WHEN p.completed = true THEN 100.0 ELSE 0.0 END
    )
    FROM UserSectionProgress p
    WHERE p.user.id = :userId
    AND p.section.course.id = :courseId
""")
    Double calculateCoursePercent(String userId, String courseId);

    Optional<UserSectionProgress>
    findByUserIdAndSectionId(String userId, String sectionId);
}
