package com.example.learningApp.configuration.batchJob.exam;

import com.example.learningApp.configuration.batchJob.BatchUtils;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Passage;
import com.example.learningApp.entity.Question;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.PassageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExamItemProcessor {

    private final ExamRepository examRepository;
    private final ExamSectionRepository sectionRepository;
    private final PassageRepository passageRepository;

    @Bean(name = "examProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, Question> examProcessor() {
        Map<String, Exam> examCache = new HashMap<>();
        Map<String, ExamSection> sectionCache = new HashMap<>();
        Map<String, Passage> passageCache = new HashMap<>();

        return row -> {
            log.info("--- Bắt đầu xử lý dòng dữ liệu: {}", row);
            if (row == null || row.get("question_type").isBlank() || row.get("question_text").isBlank()) {
                log.warn("Bỏ qua dòng này do thiếu 'question_type' hoặc 'question_text'.");
                return null;
            }

            try {
                String examCode = Optional.ofNullable(row.get("exam_code")).orElse("").trim();
                String examLevel = Optional.ofNullable(row.get("exam_level")).orElse("").trim();
                int examDuration = BatchUtils.parseIntSafe(row.get("exam_duration"), 0);
                String sectionTitle = Optional.ofNullable(row.get("section_title")).orElse("").trim();
                int sectionOrder = BatchUtils.parseIntSafe(row.get("section_order"), 0);
                int questionOrder = BatchUtils.parseIntSafe(row.get("question_order"), 0);
                String questionTypeStr = Optional.ofNullable(row.get("question_type")).orElse("").trim();

                Exam exam = examCache.computeIfAbsent(examCode, code -> {
                    log.debug("Cache miss cho Exam (code: {}). Truy vấn từ Database...", code);
                    return examRepository.findByCode(code)
                            .orElseGet(() -> {
                                log.info("Không tìm thấy Exam (code: {}). Tiến hành tạo mới Exam.", code);
                                return examRepository.save(
                                        Exam.builder()
                                                .code(code)
                                                .level(examLevel)
                                                .duration(examDuration)
                                                .sections(new ArrayList<>())
                                                .numQuestions(0)
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());
                            });
                });

                String sectionKey = sectionTitle + "_" + sectionOrder + "_" + examLevel;
                ExamSection section = sectionCache.computeIfAbsent(sectionKey,
                        key -> {
                            log.debug("Cache miss cho Section (key: {}). Truy vấn từ Database...", key);
                            return sectionRepository
                                    .findByTitleAndSectionOrderAndLevel(sectionTitle, sectionOrder, examLevel)
                                    .orElseThrow(() -> {
                                        log.error("Lỗi: Không tìm thấy Section (title={}, order={}, level={})",
                                                sectionTitle, sectionOrder, examLevel);
                                        return new IllegalStateException(
                                                "Section not found: title=" + sectionTitle
                                                        + ", order=" + sectionOrder
                                                        + ", level=" + examLevel);
                                    });
                        });

                // ⚡ Nối Exam <-> Section
                if (!exam.getSections().contains(section)) {
                    exam.getSections().add(section);
                    // Cập nhật tổng số section mỗi khi thêm mới
                    exam.setNumSections(exam.getSections().size());
                }

                if (!section.getExams().contains(exam)) {
                    section.getExams().add(exam);
                }

                AssessmentType questionType;
                try {
                    questionType = AssessmentType.valueOf(questionTypeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("Loại câu hỏi (question_type) không hợp lệ: '{}'. Bỏ qua dòng này.", questionTypeStr);
                    return null;
                }

                String answer = Optional.ofNullable(row.get("answer")).orElse("").trim();
                String optionsParsed = BatchUtils.parseOptions(row.get("options"));

                String passageTitle = Optional.ofNullable(row.get("passage_title")).orElse("").trim();
                String passageContent = Optional.ofNullable(row.get("passage_content")).orElse("").trim();
                int passageOrder = BatchUtils.parseIntSafe(row.get("passage_order"), 0);

                Passage passage = null;

                if (!passageTitle.isBlank()) {
                    String passageKey = section.getId() + "_" + passageOrder;
                    passage = passageCache.computeIfAbsent(passageKey, key -> {
                        log.info("Tạo mới Passage: {}", passageTitle);
                        Passage newPassage = Passage.builder()
                                .title(passageTitle)
                                .content(passageContent)
                                .passageOrder(passageOrder)
                                .section(section)
                                .build();
                        return passageRepository.save(newPassage);
                    });
                }

                Question question = Question.builder()
                        .section(section)
                        .passage(passage)
                        .questionType(questionType)
                        .questionText(row.get("question_text").trim())
                        .options(optionsParsed)
                        .answer(answer)
                        .explanation(Optional.ofNullable(row.get("explanation")).orElse("").trim())
                        .imageUrl(row.get("image_url"))
                        .audioUrl(row.get("audio_url"))
                        .questionOrder(questionOrder)
                        .build();

                log.info("✓ Tạo thành công Question: {}", question.getQuestionText());
                return question;

            } catch (Exception e) {
                log.error("Đã xảy ra lỗi khi xử lý dòng: {}", row, e);
                return null;
            }
        };
    }

    @Bean(name = "delayedExamProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, Question> delayedExamProcessor(
            @Qualifier("examProcessor") ItemProcessor<Map<String, String>, Question> delegate) {
        return item -> {
            if (item != null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return delegate.process(item);
        };
    }
}
