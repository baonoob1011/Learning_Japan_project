package com.example.learningApp.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabEnrichmentEvent {
    private String vocabId;
    private String surface;
    private String translated;
}
