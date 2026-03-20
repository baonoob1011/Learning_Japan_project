package com.example.learningApp.repository;

import com.example.learningApp.entity.UserCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseProgressRepository
        extends JpaRepository<UserCourseProgress, String> {
    List<UserCourseProgress> findByUserId(String userId);
    Optional<UserCourseProgress>
    findByUserIdAndCourseId(String userId, String courseId);
}

