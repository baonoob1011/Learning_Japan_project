package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateQuestionRequest {
    @NotNull
    private String sectionId;
    @NotBlank
    private String type;
    @NotBlank
    private String questionText;
    private List<String> options;
    private String answer;
    private String imageUrl;
    private String audioUrl;
    @NotNull
    private Integer sectionOrder;
}

