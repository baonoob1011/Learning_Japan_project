package com.example.learningApp.dto.response.vocab;

import java.time.LocalDateTime;

import com.example.learningApp.enums.LearningStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabResponse {
    private String id;
    private String surface; // từ gốc
    private String romaji; // phiên âm
    private String translated;
    private String reading;
    private String partOfSpeech; // loại từ
    private String targetDefs; // nghĩa ngôn ngữ đích
    private String explain;
    private String example;
    private String audioUrl; // đường dẫn audio trên S3
    private LearningStatus status; // trạng thái học tập của user hiện tại
    private LocalDateTime nextReviewAt; // Ngày hệ thống hẹn trả bài

    // Personalization fields (chỉ user hiện tại thấy)
    private String personalNote;
    private String customTranslated;
    private java.util.Set<String> personalTags;
}
