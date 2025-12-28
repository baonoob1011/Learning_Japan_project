package com.example.learningApp.dto.request.flashcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewFlashcardRequest {
    private String rating; // "AGAIN", "HARD", "GOOD", "EASY"
}
