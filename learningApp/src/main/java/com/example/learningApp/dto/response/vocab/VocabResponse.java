package com.example.learningApp.dto.response.vocab;

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
    private String surface;       // từ gốc
    private String romaji;        // phiên âm
    private String translated;
    private String reading;
    private String partOfSpeech;  // loại từ
    private String targetDefs;    // nghĩa ngôn ngữ đích
    private String explain;
    private String audioUrl;      // đường dẫn audio trên S3
}
