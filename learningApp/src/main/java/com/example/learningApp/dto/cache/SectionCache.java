package com.example.learningApp.dto.cache;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionCache {
    private String id;
    private Map<AssessmentType, Float> pointMap; // questionType -> pointPerQuestion
}