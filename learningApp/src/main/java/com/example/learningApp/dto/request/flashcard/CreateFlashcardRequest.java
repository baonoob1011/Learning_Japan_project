package com.example.learningApp.dto.request.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateFlashcardRequest {
    private String vocabId;       // ID từ vựng trong từ điển
    private String videoId;       // ID video đang xem
    private String sourceSentence; // Câu văn chứa từ đó (Quan trọng!)
}
