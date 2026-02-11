package com.example.learningApp.repository;

import com.example.learningApp.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByIsActiveTrue();
    @EntityGraph(attributePaths = {
            "sections",
            "sections.lessons",
            "sections.documents"
    })
    Optional<Course> findWithTreeById(String id);
}
