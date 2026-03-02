package com.example.learningApp.dto.cache;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionCache {
    private String id;
    private AssessmentType questionType;
    private String correctAnswer;
    private String sectionId;

    // Snapshot từ Question entity
    private String questionText;      // Nội dung câu hỏi
    private String options;           // JSON options
    private Integer questionOrder;    // Thứ tự câu hỏi
    private String answer;            // đáp án đúng
    private String explanation;       // giải thích đáp án
    private String imageUrl;          // URL ảnh
    private String audioUrl;          // URL audio     // đáp án đúng
}