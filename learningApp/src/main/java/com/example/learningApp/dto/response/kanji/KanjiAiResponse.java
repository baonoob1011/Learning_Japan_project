package com.example.learningApp.dto.response.kanji;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KanjiAiResponse {

    private String meaning;
    private String onyomi;
    private String kunyomi;
    private List<List<PointDTO>> strokeData;
}
