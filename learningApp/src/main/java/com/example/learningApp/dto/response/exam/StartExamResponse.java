package com.example.learningApp.dto.response.exam;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartExamResponse {

    private String participantId;

    private String examId;
    private String examCode;
    private Integer duration;

    private String userId;

    private Boolean completed;
    private LocalDateTime startedAt;
}

