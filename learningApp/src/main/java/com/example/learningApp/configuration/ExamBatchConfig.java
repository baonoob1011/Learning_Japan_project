package com.example.learningApp.configuration;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ExamBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ExamRepository examRepository;
    private final ExamSectionRepository sectionRepository;
    private final QuestionRepository questionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =================== READER ===================
    @Bean
    @StepScope
    public FlatFileItemReader<Map<String, String>> reader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);
        reader.setEncoding("UTF-8");

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(",");
                setStrict(false);
                setNames("exam_code","exam_level","exam_duration","section_title","section_order",
                        "question_type","question_text","options","answer","image_url","audio_url","question_order");
            }});
            setFieldSetMapper(fieldSet -> {
                Map<String, String> map = new HashMap<>();
                for (String name : fieldSet.getNames()) {
                    map.put(name, fieldSet.readString(name));
                }
                return map;
            });
        }});

        return reader;
    }

    // =================== PROCESSOR ===================
    @Bean
    public ItemProcessor<Map<String, String>, Question> processor() {
        return row -> {
            try {
                // --- Parse safely ---
                String examCode = row.get("exam_code");
                String examLevel = row.get("exam_level");
                int examDuration = parseIntSafe(row.get("exam_duration"), 0);
                String sectionTitle = row.get("section_title");
                int sectionOrder = parseIntSafe(row.get("section_order"), 0);
                int questionOrder = parseIntSafe(row.get("question_order"), 0);

                // --- Get or create Exam ---
                Exam exam = examRepository.findByCode(examCode)
                        .orElseGet(() -> examRepository.save(
                                Exam.builder()
                                        .code(examCode)
                                        .level(examLevel)
                                        .duration(examDuration)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ));

                // --- Get or create Section ---
                ExamSection section = sectionRepository.findByExamAndTitle(exam, sectionTitle)
                        .orElseGet(() -> sectionRepository.save(
                                ExamSection.builder()
                                        .exam(exam)
                                        .title(sectionTitle)
                                        .orderNum(sectionOrder)
                                        .build()
                        ));

                // --- Parse options to JSON-safe string ---
                String optionsStr = row.get("options");
                String optionsJson;
                if (optionsStr == null || optionsStr.isBlank()) {
                    optionsJson = "null"; // safe for jsonb
                } else {
                    try {
                        optionsJson = objectMapper.writeValueAsString(objectMapper.readTree(optionsStr));
                    } catch (Exception e) {
                        optionsJson = objectMapper.writeValueAsString(optionsStr);
                        System.err.println("Warning: invalid JSON for options '" + optionsStr + "', storing as string");
                    }
                }

                // --- Parse answer to JSON-safe string ---
                String answerStr = row.get("answer");
                String answerJson;
                if (answerStr == null || answerStr.isBlank()) {
                    answerJson = "null"; // safe for jsonb
                } else {
                    try {
                        answerJson = objectMapper.writeValueAsString(objectMapper.readTree(answerStr));
                    } catch (Exception e) {
                        answerJson = objectMapper.writeValueAsString(answerStr);
                        System.err.println("Warning: invalid JSON for answer '" + answerStr + "', storing as string");
                    }
                }

                // --- Build and return Question ---
                return Question.builder()
                        .section(section)
                        .type(row.get("question_type"))
                        .questionText(row.get("question_text"))
                        .options(optionsJson)
                        .answer(answerJson)
                        .imageUrl(row.get("image_url"))
                        .audioUrl(row.get("audio_url"))
                        .orderNum(questionOrder)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                System.err.println("Error processing row: " + row);
                e.printStackTrace();
                return null; // skip invalid row
            }
        };
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) return defaultValue;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Warning: invalid number '" + value + "', using default " + defaultValue);
            return defaultValue;
        }
    }

    // =================== WRITER ===================
    @Bean
    public ItemWriter<Question> writer() {
        return questions -> {
            var questionList = StreamSupport.stream(questions.spliterator(), false)
                    .filter(q -> q != null)
                    .collect(Collectors.toList());
            questionRepository.saveAll(questionList);
        };
    }

    // =================== STEP ===================
    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Map<String, String>, Question>chunk(10, transactionManager)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .build();
    }

    // =================== JOB ===================
    @Bean
    public Job importExamJob() {
        return new JobBuilder("importExamJob", jobRepository)
                .start(step1())
                .build();
    }
}
