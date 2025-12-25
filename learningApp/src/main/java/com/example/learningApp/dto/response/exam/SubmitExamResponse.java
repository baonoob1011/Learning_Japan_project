package com.example.learningApp.dto.response.exam;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitExamResponse {

    private String participantId;
    private String examId;
    private String examCode;
    private String aiReview;

    private Float totalScore;
    private Boolean completed;
    private int answeredCount;
    private int totalQuestions;
    private int correctCount;
    private int skippedCount;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    private List<AnswerDetail> answers;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerDetail {
        private String questionId;
        private String questionText;
        private String questionType;
        private String optionsJson;       // lưu JSON nguyên bản
        private String correctAnswer;
        private Integer sectionDuration;
        private String answer;
        private Boolean isCorrect;
        private Float score;
        private Integer questionOrder;
        private Integer sectionOrder;
        private String sectionTitle;
        private String explanation;
        private String imageUrl;
        private String audioUrl;
    }
}
