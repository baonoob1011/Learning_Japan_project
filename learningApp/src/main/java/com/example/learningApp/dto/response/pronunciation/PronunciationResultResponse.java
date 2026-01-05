package com.example.learningApp.dto.response.pronunciation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationResultResponse {
    private String recognizedText;

    /** Câu / từ chuẩn (đáp án) */
    private String expectedText;

    /** Điểm tổng (0 → 1) */
    private double accuracy;

    /** Kết quả theo từng từ */
    private List<WordPronunciationResultResponse> wordResults;

    /** Nhận xét tổng quát (optional) */
    private String feedback;
}
