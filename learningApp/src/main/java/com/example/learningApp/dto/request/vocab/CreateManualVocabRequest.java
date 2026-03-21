package com.example.learningApp.dto.request.vocab;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateManualVocabRequest {
    @NotBlank
    private String surface;

    private String translated;
    private String reading;
    private String romaji;
    private String partOfSpeech;
}
