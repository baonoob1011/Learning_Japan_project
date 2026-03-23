package com.example.learningApp.dto.request.vocab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVocabRequest {
    private String videoId; // từ gốc
    private String surface; // từ gốc
    private String romaji; // phiên âm
    private String translated;
    private String reading;
    private String partOfSpeech; // loại từ
    private String targetDefs; // nghĩa ngôn ngữ đích
    private String explain; // (legacy) ví dụ dạng text thô
    private String example; // ví dụ minh họa (AI generated, lưu vào DB)
    private String userId; // ID của user (để lưu vào User.savedVocabs)
    private String audioUrl; // đường dẫn audio S3 (nếu đã tạo sẵn)

}
