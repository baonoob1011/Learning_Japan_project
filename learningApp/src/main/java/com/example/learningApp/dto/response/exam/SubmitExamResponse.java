package com.example.learningApp.dto.response.exam;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitExamResponse {

    private String participantId;
    private String examId;
    private String examCode;

    private Float score;
    private Boolean completed;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
