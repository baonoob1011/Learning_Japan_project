package com.example.learningApp.configuration.batchJob.section;

import com.example.learningApp.configuration.batchJob.BatchUtils;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.AssessmentItemRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ExamSectionItemProcessor {

    private final ExamSectionRepository sectionRepository;
    private final AssessmentItemRepository assessmentItemRepository;

    @Bean(name = "examSectionProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, AssessmentItem> examSectionProcessor() {

        // Cache các section và item key đã thấy trong lượt chạy này để chặn trùng tuyệt
        // đối
        Map<String, ExamSection> sectionCache = new ConcurrentHashMap<>();
        Set<String> processedItemKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

        return row -> {

            if (row == null || row.get("assessment_type") == null || row.get("assessment_type").isBlank()) {
                return null;
            }

            try {
                // Chuẩn hóa dữ liệu đầu vào (Trim và viết hoa)
                String sectionTitle = row.get("section_title").trim();
                int sectionOrder = BatchUtils.parseIntSafe(row.get("section_order"), 0);
                int sectionDuration = BatchUtils.parseIntSafe(row.get("section_duration"), 0);
                String sectionLevel = row.get("assessment_level").trim().toUpperCase();
                String assessmentName = row.get("assessment_name").trim();

                String sectionKey = (sectionTitle + "_" + sectionOrder + "_" + sectionLevel).replaceAll("\\s+", "_");

                // 1. Tìm hoặc Tạo Section
                ExamSection section = sectionCache.computeIfAbsent(sectionKey, key -> sectionRepository
                        .findByTitleAndSectionOrderAndLevel(sectionTitle, sectionOrder, sectionLevel)
                        .orElseGet(() -> {
                            ExamSection newSection = ExamSection.builder()
                                    .title(sectionTitle)
                                    .sectionOrder(sectionOrder)
                                    .level(sectionLevel)
                                    .sectionDuration(sectionDuration)
                                    .assessmentItems(new LinkedHashSet<>())
                                    .passages(new LinkedHashSet<>())
                                    .questions(new LinkedHashSet<>())
                                    .build();
                            return sectionRepository.save(newSection);
                        }));

                // 2. Kiểm tra trùng Mondai (AssessmentItem)
                AssessmentType type = AssessmentType.valueOf(row.get("assessment_type").trim());

                // Key định danh duy nhất cho 1 loại bài trong 1 Section
                String itemKey = sectionKey + "##" + type.name();

                // Kiểm tra trong Local Cache hoặc Database
                if (processedItemKeys.contains(itemKey) ||
                        assessmentItemRepository.existsBySectionAndAssessmentType(section, type)) {
                    // System.out.println("⏭️ Bỏ qua Mondai trùng lặp: " + type + " ở level " +
                    // sectionLevel);
                    return null;
                }

                // Đánh dấu đã xử lý để dòng tiếp theo trong cùng lượt chạy không bị trùng
                processedItemKeys.add(itemKey);

                int questionCount = BatchUtils.parseIntSafe(row.get("question_count"), 0);
                float pointPerQuestion = BatchUtils.parseFloatSafe(row.get("point_per_question"), 0f);

                // 3. Tạo Item
                return AssessmentItem.builder()
                        .section(section)
                        .assessmentType(type)
                        .name(assessmentName)
                        .level(sectionLevel)
                        .questionCount(questionCount)
                        .pointPerQuestion(pointPerQuestion)
                        .totalPoint(questionCount * pointPerQuestion)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                System.err.println("❌ Lỗi xử lý dòng: " + row);
                return null;
            }
        };
    }
}
