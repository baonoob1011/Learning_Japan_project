package com.example.learningApp.dto.request.vocab;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyVocabRequest {
    private String vocabId;
    private boolean mastered;
}
