package com.example.learningApp.configuration.batchJob.section;

import com.example.learningApp.configuration.batchJob.BatchUtils;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExamSectionItemProcessor {

    private final ExamSectionRepository sectionRepository;

    @Bean(name = "examSectionProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, ExamSection> examSectionProcessor() {

        Map<String, ExamSection> sectionCache = new HashMap<>();

        return row -> {

            // ================== SKIP ROW RỖNG ==================
            if (row == null
                    || row.get("assessment_type") == null
                    || row.get("assessment_type").isBlank()) {
                return null;
            }

            try {
                String sectionTitle = row.get("section_title").trim();
                int sectionOrder = BatchUtils.parseIntSafe(row.get("section_order"), 0);
                int sectionDuration = BatchUtils.parseIntSafe(row.get("section_duration"), 0);
                String sectionLevel = row.get("assessment_level").trim();

                String sectionKey = sectionTitle + "_" + sectionOrder + "_" + sectionLevel;

                // ================== LOAD / CREATE SECTION ==================
                ExamSection section = sectionCache.computeIfAbsent(sectionKey, key -> sectionRepository
                        .findByTitleAndSectionOrderAndLevel(
                                sectionTitle,
                                sectionOrder,
                                sectionLevel)
                        .orElseGet(() -> ExamSection.builder()
                                .title(sectionTitle)
                                .sectionOrder(sectionOrder)
                                .level(sectionLevel)
                                .sectionDuration(sectionDuration)
                                .assessmentItems(new ArrayList<>())
                                .questions(new ArrayList<>())
                                .build()));

                // ================== PARSE ENUM SAFE ==================
                AssessmentType type = AssessmentType.valueOf(
                        row.get("assessment_type").trim());

                int questionCount = BatchUtils.parseIntSafe(row.get("question_count"), 0);
                float pointPerQuestion = BatchUtils.parseFloatSafe(row.get("point_per_question"), 0f);

                // ================== CREATE ASSESSMENT ITEM ==================
                AssessmentItem item = AssessmentItem.builder()
                        .section(section)
                        .assessmentType(type)
                        .name(row.get("assessment_name").trim())
                        .level(sectionLevel)
                        .questionCount(questionCount)
                        .pointPerQuestion(pointPerQuestion)
                        .totalPoint(questionCount * pointPerQuestion)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                section.getAssessmentItems().add(item);

                return section;

            } catch (Exception e) {
                System.err.println("❌ Error row: " + row);
                e.printStackTrace();
                return null; // ❗ không fail job
            }
        };
    }
}
