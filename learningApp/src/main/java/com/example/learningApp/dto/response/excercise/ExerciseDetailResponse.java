package com.example.learningApp.dto.response.excercise;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ExerciseDetailResponse {
    private String id;
    private String videoId;
    private String title;
    private String description;
    private Integer totalQuestions;
    private Instant createdAt;
    private List<QuestionResponse> questions; // <-- thêm dòng này

}
