package com.example.learningApp.dto.request.kanji;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateKanjiRequest {
    private String character;
    private String meaning;
    private String onyomi;
    private String kunyomi;
}
