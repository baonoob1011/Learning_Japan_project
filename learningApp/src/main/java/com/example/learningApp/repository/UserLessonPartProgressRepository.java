package com.example.learningApp.repository;

import com.example.learningApp.entity.UserLessonPartProgress;
import com.example.learningApp.entity.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLessonPartProgressRepository
        extends JpaRepository<UserLessonPartProgress, String> {

    Optional<UserLessonPartProgress>
    findByUserIdAndLessonPartId(String userId, String lessonPartId);

    // 🔥 Tính trung bình progress của lesson
    @Query("""
        SELECT AVG(p.progressPercent)
        FROM UserLessonPartProgress p
        WHERE p.user.id = :userId
        AND p.lessonPart.lesson.id = :lessonId
    """)
    Double calculateLessonPercent(String userId, String lessonId);
}
