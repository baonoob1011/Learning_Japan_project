package com.example.learningApp.dto.response.exam;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SectionWithQuestionsResponse {
    private String id;        // section id
    private String examId;
    private String title;
    private Integer sectionDuration;
    private Integer sectionOrder;
    private List<QuestionItem> questions; // danh sách question trong section

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class QuestionItem {
        private String id;
        private Integer sectionOrder;
        private AssessmentType questionType;
        private String questionText;
        private String options;
        private String answer;
        private String imageUrl;
        private String audioUrl;
        private Integer questionOrder;
    }
}
