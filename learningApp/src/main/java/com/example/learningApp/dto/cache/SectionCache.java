package com.example.learningApp.dto.cache;

import com.example.learningApp.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionCache {
    private String id;
    private String title;          // thêm field title
    private Integer sectionOrder;  // giữ nguyên sectionOrder
    private Integer sectionDuration;
    private Map<AssessmentType, Float> pointMap;
}