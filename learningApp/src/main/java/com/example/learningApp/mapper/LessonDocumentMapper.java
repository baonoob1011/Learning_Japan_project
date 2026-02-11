package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.lesson.CreateLessonDocumentRequest;
import com.example.learningApp.dto.request.lesson.CreateLessonPartRequest;
import com.example.learningApp.dto.response.lesson.LessonDocumentResponse;
import com.example.learningApp.dto.response.lesson.LessonPartResponse;
import com.example.learningApp.entity.LessonDocument;
import com.example.learningApp.entity.LessonPart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonDocumentMapper {
    LessonDocument toLessonDocument(CreateLessonDocumentRequest createLessonDocumentRequest);
    LessonDocumentResponse toLessonDocumentResponse(LessonDocument lessonDocument);
}
