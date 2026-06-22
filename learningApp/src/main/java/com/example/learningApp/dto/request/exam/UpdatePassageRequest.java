package com.example.learningApp.dto.request.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePassageRequest {

    @NotBlank(message = "Passage title is required")
    private String title;

    @NotBlank(message = "Passage content is required")
    private String content;

    private Integer passageOrder;
}


