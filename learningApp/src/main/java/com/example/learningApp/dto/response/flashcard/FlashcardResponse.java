package com.example.learningApp.dto.response.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FlashcardResponse {
    private String id;            // ID của thẻ (UserFlashcard)
    private String word;        // Kanji
    private String hiragana;    // Cách đọc
    private String meaning;     // Nghĩa
    private String sourceSentence; // Câu ví dụ
    private String urlVideo;
    private String videoId;       // Để làm tính năng "Quay lại video"
}
