package com.example.learningApp.dto.response.excercise;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class ExerciseResponse {
    private String exerciseId;
    private Integer totalQuestions;
}
