package com.example.learningApp.dto.response.excercise;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private String questionId;
    private String transcriptText;
    private String questionText;
    private String questionType;
    private List<OptionResponse> options;
}
