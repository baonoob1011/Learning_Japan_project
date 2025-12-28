package com.example.learningApp.dto.response.translate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslateResponse {
    private String original;
    private String translated;
    private String reading;
    private String romaji;
    private String explanation;

    private List<Example> examples;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Example {
        private String jp;
        private String vi;
    }
}
