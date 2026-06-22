package com.example.learningApp.dto.request.exam;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartExamRequest {

    @NotBlank(message = "Exam ID must not be blank")
    private String examId;

}

