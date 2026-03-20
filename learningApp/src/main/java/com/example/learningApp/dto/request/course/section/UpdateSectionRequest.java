package com.example.learningApp.dto.request.course.section;


import com.example.learningApp.enums.LessonLevel;
import lombok.Data;

@Data
public class UpdateSectionRequest {

    private String title;

    private LessonLevel lessonLevel;
}
