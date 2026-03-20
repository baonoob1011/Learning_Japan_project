package com.example.learningApp.dto.request.course.section.lesson;

import com.example.learningApp.enums.LessonPartType;
import lombok.Data;

@Data
public class CreateLessonPartRequest {

    private String lessonId;
    private LessonPartType lessonPartType;
    private String title;
    private String videoUrl;
    private Integer partOrder;
}


