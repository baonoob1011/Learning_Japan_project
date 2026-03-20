package com.example.learningApp.dto.response.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SkillProgressResponse {

    private double vocabulary;
    private double grammar;
    private double reading;
    private double listening;
    private double kanji;
}
