package com.example.learningApp.repository;

import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    @Query("""
       SELECT e.course
       FROM Enrollment e
       WHERE e.user.id = :userId
       """)
    List<Course> findCoursesByUserId(String userId);
}

