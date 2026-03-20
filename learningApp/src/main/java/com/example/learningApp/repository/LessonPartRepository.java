package com.example.learningApp.repository;

import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.LessonPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonPartRepository extends JpaRepository<LessonPart, String> {
    List<LessonPart> findByLesson(Lesson lesson);
    List<LessonPart> findByLessonId(String lessonId);

}

