package com.example.learningApp.dto.response.excercise;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionResponse {
    private Integer optionIndex;
    private boolean correct;
    private String content;
}