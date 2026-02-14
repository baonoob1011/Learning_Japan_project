package com.example.learningApp.dto.request.kanji;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateKanjiRequest {

    private String character;
    private String meaning;
    private String onyomi;
    private String kunyomi;

    // JSON string stroke
    private String strokeData;
}
