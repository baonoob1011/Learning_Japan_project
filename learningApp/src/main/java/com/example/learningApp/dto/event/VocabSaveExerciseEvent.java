package com.example.learningApp.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabSaveExerciseEvent {
    private String userId;
    private String vocabId;
    private String surface;
}

