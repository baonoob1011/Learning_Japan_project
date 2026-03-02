package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.course.section.lesson.CreateLessonDocumentRequest;
import com.example.learningApp.dto.response.lesson.LessonDocumentResponse;
import com.example.learningApp.entity.LessonDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonDocumentMapper {
    LessonDocument toLessonDocument(CreateLessonDocumentRequest createLessonDocumentRequest);
    LessonDocumentResponse toLessonDocumentResponse(LessonDocument lessonDocument);
}
