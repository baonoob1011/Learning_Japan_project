package com.example.learningApp.dto.request.lesson;

import lombok.Data;

@Data
public class CreateLessonDocumentRequest {

    private String lessonId;
    private String title;
    private Integer documentOrder;
}
