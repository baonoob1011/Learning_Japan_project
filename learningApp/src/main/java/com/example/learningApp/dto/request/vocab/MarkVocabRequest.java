package com.example.learningApp.dto.request.vocab;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkVocabRequest {
        private boolean remembered; // true = thuộc, false = chưa thuộc
    private String vocabId;
}
