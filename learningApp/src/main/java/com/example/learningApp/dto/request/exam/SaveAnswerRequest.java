package com.example.learningApp.dto.request.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAnswerRequest {

    @NotBlank
    private String participantId;

    @NotBlank
    private String questionId;

    @NotBlank
    private String answer;
}
