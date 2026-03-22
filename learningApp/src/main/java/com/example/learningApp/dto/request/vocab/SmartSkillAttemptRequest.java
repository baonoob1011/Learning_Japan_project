package com.example.learningApp.dto.request.vocab;

import com.example.learningApp.enums.StudyMode;
import com.example.learningApp.enums.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartSkillAttemptRequest {
    private String vocabId;
    private Skill skill; // New field for mixed question queue
    private StudyMode studyMode;
    private boolean success;
}
