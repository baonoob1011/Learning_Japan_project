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

                // 2. Kiểm tra trùng hoặc Cập nhật Mondai (AssessmentItem)
                AssessmentType type = AssessmentType.valueOf(row.get("assessment_type").trim());

                // Key định danh duy nhất cho 1 loại bài trong 1 Section (Cache trong loop)
                String itemKey = sectionKey + "##" + type.name();
                if (processedItemKeys.contains(itemKey)) {
                    return null;
                }
                processedItemKeys.add(itemKey);

                // Tìm xem đã có Mondai này trong Database chưa để cập nhật
                AssessmentItem assessmentItem = assessmentItemRepository
                        .findBySectionAndAssessmentType(section, type)
                        .orElse(AssessmentItem.builder()
                                .section(section)
                                .assessmentType(type)
                                .createdAt(LocalDateTime.now())
                                .build());

                int questionCount = BatchUtils.parseIntSafe(row.get("question_count"), 0);
                float totalPoints = BatchUtils.parseFloatSafe(row.get("total_points"), 0f);
                float pointPerQuestion = BatchUtils.parseFloatSafe(row.get("point_per_question"), 0f);

                // Ưu tiên tính pointPerQuestion chuẩn xác từ totalPoints nếu có
                if (totalPoints > 0 && questionCount > 0) {
                    float calculatedPoint = totalPoints / questionCount;
                    if (pointPerQuestion == 0 || Math.abs(pointPerQuestion - calculatedPoint) > 0.0001) {
                        pointPerQuestion = calculatedPoint;
                    }
                } else if (pointPerQuestion > 0 && questionCount > 0) {
                    totalPoints = questionCount * pointPerQuestion;
                }

                // 3. Cập nhật các trường
                assessmentItem.setName(assessmentName);
                assessmentItem.setLevel(sectionLevel);
                assessmentItem.setQuestionCount(questionCount);
                assessmentItem.setPointPerQuestion(pointPerQuestion);
                assessmentItem.setTotalPoint(totalPoints);
                assessmentItem.setUpdatedAt(LocalDateTime.now());

                return assessmentItem;

            } catch (Exception e) {
                System.err.println("❌ Lỗi xử lý dòng: " + row);
                return null;
            }
        };
    }
}

