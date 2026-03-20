package com.example.learningApp.dto.response.vocab;


import com.example.learningApp.enums.LearningStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVocabProgressResponse {

    private String vocabId;
    private String vocabWord;
    private LearningStatus status;

    private int reviewCount;
    private int forgottenCount;

    private LocalDateTime lastReviewedAt;
    private LocalDateTime createdAt;
}
