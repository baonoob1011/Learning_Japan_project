package com.example.learningApp.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabCache {
    private String id; // ID của vocab
    private String surface; // từ gốc
    private String romaji; // phiên âm
    private String translated; // nghĩa
    private String reading; // cách đọc
    private String targetDefs; // nghĩa ngôn ngữ đích
    private String partOfSpeech; // loại từ
    private String audioUrl; // đường dẫn audio trên S3
    private String exampleJa;
    private String exampleVi;
    private String example;
}
