package com.example.learningApp.repository;

import com.example.learningApp.entity.UserSectionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSectionProgressRepository
        extends JpaRepository<UserSectionProgress, String> {
    @Query("""
    SELECT 
        (SUM(COALESCE(usp.progressPercent, 0)) / COUNT(s))
    FROM Section s
    LEFT JOIN UserSectionProgress usp
        ON usp.section.id = s.id
        AND usp.user.id = :userId
    WHERE s.course.id = :courseId
""")
    Double calculateCoursePercent(String userId, String courseId);


    Optional<UserSectionProgress>
    findByUserIdAndSectionId(String userId, String sectionId);
}
