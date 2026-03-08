package com.example.learningApp.dto.request.exam.question;

import lombok.Data;

import java.util.List;

@Data
public class UpdateQuestionRequest {

    private String sectionId;
    private String questionType;
    private String questionText;
    private List<String> options;
    private String answer;
    private String imageUrl;
    private String audioUrl;
    private Integer questionOrder;
}