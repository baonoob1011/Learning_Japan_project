package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.lesson.CreateLessonPartRequest;
import com.example.learningApp.dto.response.lesson.LessonPartResponse;
import com.example.learningApp.entity.LessonPart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LessonPartMapper {
    LessonPart toLessonPart(CreateLessonPartRequest lessonPart);
    LessonPartResponse toLessonPartResponse(LessonPart lessonPart);
}
