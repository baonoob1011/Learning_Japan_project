package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.course.CreateCourseRequest;
import com.example.learningApp.dto.request.lesson.CreateLessonRequest;
import com.example.learningApp.dto.response.lesson.LessonResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.Lesson;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface LessonMapper {
    Lesson toLesson(CreateLessonRequest createLessonRequest);
    LessonResponse toLessonResponse(Lesson lesson);
}
