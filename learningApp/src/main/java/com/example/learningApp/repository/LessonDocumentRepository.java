package com.example.learningApp.repository;

import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.LessonDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonDocumentRepository extends JpaRepository<LessonDocument, String> {

    List<LessonDocument> findByLessonOrderByDocumentOrderAsc(Lesson lesson);
}
