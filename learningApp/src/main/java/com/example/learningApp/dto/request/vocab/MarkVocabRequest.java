package com.example.learningApp.dto.request.vocab;

import com.example.learningApp.enums.StudyMode;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkVocabRequest {
    private String vocabId;
    private boolean remembered; 
    
    @Builder.Default
    private StudyMode studyMode = StudyMode.FLASHCARD;
}
