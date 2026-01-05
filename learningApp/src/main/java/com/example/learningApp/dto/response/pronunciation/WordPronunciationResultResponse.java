package com.example.learningApp.dto.response.pronunciation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordPronunciationResultResponse {
    private String word;

    /** Từ người dùng đọc được */
    private String spokenWord;

    /** Đúng hay sai */
    private boolean correct;

    /** Điểm của từ (0 → 1) */
    private double score;
}
