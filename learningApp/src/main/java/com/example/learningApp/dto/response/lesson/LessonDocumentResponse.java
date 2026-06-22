package com.example.learningApp.dto.response.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LessonDocumentResponse {
    private String id;
    private String title;
    private String documentUrl;
    private Integer documentOrder;
}

