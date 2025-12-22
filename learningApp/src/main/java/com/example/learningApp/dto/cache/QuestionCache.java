package com.example.learningApp.dto.cache;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCache {
    private String id;
    private AssessmentType questionType;
    private String correctAnswer;
    private String sectionId;

    // Snapshot từ Question entity
    private String questionText;  // TEXT của câu hỏi
    private String options;       // JSON options
    private Integer orderNum;     // Thứ tự câu hỏi
    private String answer;        // đáp án đúng
}