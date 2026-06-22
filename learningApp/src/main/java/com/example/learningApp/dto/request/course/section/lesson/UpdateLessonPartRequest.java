package com.example.learningApp.dto.request.course.section.lesson;

import com.example.learningApp.enums.LessonPartType;
import lombok.Data;

@Data
public class UpdateLessonPartRequest {

    private LessonPartType lessonPartType;

    private String title;

    private String videoUrl;

    private Integer partOrder;
}
