package com.example.learningApp.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWordRequest {
    @NotBlank
    private String surface;
}

