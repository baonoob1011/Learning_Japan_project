package com.example.learningApp.dto.request.course.section.lesson;

import lombok.Data;

@Data
public class CreateLessonDocumentRequest {

    private String lessonId;
    private String title;
    private Integer documentOrder;
}

