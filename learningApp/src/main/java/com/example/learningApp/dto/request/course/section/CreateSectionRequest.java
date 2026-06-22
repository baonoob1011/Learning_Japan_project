package com.example.learningApp.dto.request.course.section;

import com.example.learningApp.enums.LessonLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSectionRequest {

    private String courseId;

    private String title;

}

