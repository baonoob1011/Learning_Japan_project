package com.example.learningApp.dto.response.vocab;

import com.example.learningApp.enums.LearningStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VocabStatusResponse {
    private String vocabId;
    private LearningStatus status;
}
