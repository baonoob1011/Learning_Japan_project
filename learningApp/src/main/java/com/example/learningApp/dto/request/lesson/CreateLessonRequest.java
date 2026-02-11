package com.example.learningApp.dto.request.lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLessonRequest {

    private String sectionId;
    private String title;
    private String description;
    private Integer lessonOrder;
}
