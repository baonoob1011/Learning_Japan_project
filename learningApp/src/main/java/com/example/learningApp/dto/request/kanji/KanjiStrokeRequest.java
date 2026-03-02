package com.example.learningApp.dto.request.kanji;

import com.example.learningApp.dto.response.kanji.PointDTO;
import lombok.Data;

import java.util.List;

@Data
public class KanjiStrokeRequest {

    private String kanjiId;

    private List<List<PointDTO>> strokes;
}
