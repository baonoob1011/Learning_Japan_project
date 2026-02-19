package com.example.learningApp.dto.response.kanji;

import com.example.learningApp.dto.PointDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KanjiResponse {

    private String id;
    private String character;   // 漢

    private String meaning;     // Chinese
    private String onyomi;
    private String kunyomi;

    private List<List<PointDTO>> strokes; // parse từ JSON
}
