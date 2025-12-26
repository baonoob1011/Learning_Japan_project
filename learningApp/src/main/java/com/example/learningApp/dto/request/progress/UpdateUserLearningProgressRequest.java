package com.example.learningApp.dto.request.progress;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserLearningProgressRequest {

    @NotBlank
    private String userId;
    private String level;
    @Min(1)
    private Integer totalQuestions;
    @Min(0)
    private Integer correctQuestions;
}
