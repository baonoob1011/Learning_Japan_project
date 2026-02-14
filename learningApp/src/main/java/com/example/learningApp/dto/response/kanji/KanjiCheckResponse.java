package com.example.learningApp.dto.response.kanji;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KanjiCheckResponse {

    private boolean correct;
    private double score;
    private int expectedStrokeCount;
    private int userStrokeCount;
}
