package com.example.learningApp.dto.request.excercise;

import lombok.Data;

@Data
public class GenerateExerciseRequest {
    private String videoId;
    private String title;
    private String description;
}