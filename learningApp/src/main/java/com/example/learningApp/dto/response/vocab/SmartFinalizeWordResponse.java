package com.example.learningApp.dto.response.vocab;

import com.example.learningApp.enums.ReviewGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartFinalizeWordResponse {
    private String vocabId;
    private String wordProgressId;
    private ReviewGrade grade;
    private int wrongCount;
    private LocalDateTime nextReviewAt;
}
