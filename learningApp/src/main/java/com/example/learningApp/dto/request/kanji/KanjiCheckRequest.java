package com.example.learningApp.dto.request.kanji;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KanjiCheckRequest {

    private String kanjiId;
    private String userStrokeData; // JSON stroke user vẽ
}

