package com.example.learningApp.repository;

import com.example.learningApp.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findBySectionIdOrderByLessonOrderAsc(String sectionId);

}
