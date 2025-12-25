package com.example.learningApp.dto.cache;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamAnswerCache {
    private String questionId;
    private String questionText;
    private AssessmentType questionType;
    private String options;
    private int sectionOrder;
    private String correctAnswer;

    private String answer;
    private boolean isCorrect;
    private float score;
    private LocalDateTime answeredAt;
}
