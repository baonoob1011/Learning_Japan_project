package com.example.learningApp.dto.request.vocab;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartFinalizeWordRequest {
    private String vocabId;
    private int wrongCount;
    private boolean failedInRetry;
}
