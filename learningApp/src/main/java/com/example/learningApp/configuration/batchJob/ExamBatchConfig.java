package com.example.learningApp.configuration.batchJob;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.*;
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
    private final AmazonS3 amazonS3;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // =====================================================
    // READER: LOAD FILE CSV FROM S3 (exam/)
    // =====================================================
    @Bean(name = "examReader")
    @StepScope
    public MultiResourceItemReader<Map<String, String>> examReader(
            @Value("#{jobParameters['exam']}") String examKey
    ) {

        MultiResourceItemReader<Map<String, String>> reader =
                new MultiResourceItemReader<>();

        reader.setResources(loadExamFilesFromS3(examKey));
        reader.setDelegate(singleExamCsvReader());

        return reader;
    }

    // =====================================================
    // SINGLE CSV READER
    // =====================================================
    @Bean
    public FlatFileItemReader<Map<String, String>> singleExamCsvReader() {

        FlatFileItemReader<Map<String, String>> reader =
                new FlatFileItemReader<>();

        reader.setLinesToSkip(1);
        reader.setEncoding("UTF-8");

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(",");
                setStrict(false);
                setNames(
                        "exam_code","exam_level","exam_duration",
                        "section_title","section_order",
                        "question_type","question_text",
                        "options","answer","explanation",
                        "image_url","audio_url","question_order"
                );
            }});
            setFieldSetMapper(fs -> {
                Map<String, String> map = new HashMap<>();
                for (String name : fs.getNames()) {
                    map.put(name, fs.readString(name));
                }
                return map;
            });
        }});

        return reader;
    }

    // =====================================================
    // LOAD FILE EXAM/*.CSV FROM S3
    // =====================================================
    private Resource[] loadExamFilesFromS3(String examKey) {

        List<Resource> resources = new ArrayList<>();

        List<S3ObjectSummary> objects =
                amazonS3.listObjects(bucketName, "exam/")
                        .getObjectSummaries();

        for (S3ObjectSummary obj : objects) {

            if (!obj.getKey().endsWith(".csv")) continue;

            // Nếu truyền examKey thì chỉ load đúng file đó
            if (examKey != null && !obj.getKey().contains(examKey)) continue;

            try {
                String url = amazonS3
                        .getUrl(bucketName, obj.getKey())
                        .toString();

                System.out.println("📥 Import exam file: " + obj.getKey());
                resources.add(new UrlResource(url));

            } catch (Exception e) {
                throw new RuntimeException("Cannot load exam file: " + obj.getKey(), e);
            }
        }

        if (resources.isEmpty()) {
            throw new IllegalStateException(
                    "❌ Không tìm thấy file CSV trong S3 folder exam/");
        }

        return resources.toArray(new Resource[0]);
    }

    // =====================================================
    // PROCESSOR
    // =====================================================
    @Bean(name = "examProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, Question> examProcessor() {

        return row -> {
            if (row == null || row.get("question_type") == null || row.get("question_type").isBlank()
                    || row.get("question_text") == null || row.get("question_text").isBlank()) {
                return null;
            }

            try {
                String examCode = row.get("exam_code").trim();
                String examLevel = row.get("exam_level").trim();
                int examDuration = parseIntSafe(row.get("exam_duration"), 0);

                String sectionTitle = row.get("section_title").trim();
                int sectionOrder = parseIntSafe(row.get("section_order"), 0);
                int questionOrder = parseIntSafe(row.get("question_order"), 0);

                // Parse enum
                AssessmentType questionType;
                try {
                    questionType = AssessmentType.valueOf(row.get("question_type").trim());
                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Invalid question_type: " + row.get("question_type") + ", skip row.");
                    return null;
                }

                // Exam
                Exam exam = examRepository.findByCode(examCode)
                        .orElseGet(() -> examRepository.save(
                                Exam.builder()
                                        .code(examCode)
                                        .level(examLevel)
                                        .duration(examDuration)
                                        .sections(new ArrayList<>())
                                        .questions(new ArrayList<>())
                                        .numSections(0)
                                        .numQuestions(0)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ));

                // Section
                ExamSection section = sectionRepository
                        .findByTitleAndOrderNumAndLevel(sectionTitle, sectionOrder, examLevel)
                        .orElseThrow(() -> new IllegalStateException(
                                "❌ Section NOT FOUND: " + sectionTitle + " | order=" + sectionOrder + " | level=" + examLevel
                        ));

                // Add section to exam if not exists
                if (!exam.getSections().contains(section)) {
                    exam.getSections().add(section);
                    section.getExams().add(exam);
                    exam.setNumSections(exam.getSections().size());
                    examRepository.save(exam);
                }

                // Create question
                Question question = Question.builder()
                        .section(section)
                        .questionType(questionType)
                        .questionText(row.get("question_text").trim())
                        .options(toJson(row.get("options")))
                        .answer(toJson(row.get("answer")))
                        .explanation(Optional.ofNullable(row.get("explanation")).orElse(""))
                        .imageUrl(row.get("image_url"))
                        .audioUrl(row.get("audio_url"))
                        .orderNum(questionOrder)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                // Gắn exam Many-to-Many
                question.getExams().add(exam);
                exam.getQuestions().add(question);

                return question;

            } catch (Exception e) {
                System.err.println("❌ Error row: " + row);
                e.printStackTrace();
                return null;
            }
        };
    }


    // =====================================================
    // WRITER
    // =====================================================
    @Bean(name = "examWriter")
    public ItemWriter<Question> examWriter() {
        return items -> {
            List<Question> list = StreamSupport.stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            questionRepository.saveAll(list);

            // Lấy tất cả exam liên quan trong batch này
            Set<Exam> exams = list.stream()
                    .flatMap(q -> q.getExams().stream())
                    .collect(Collectors.toSet());

            // Cập nhật numQuestions cho mỗi exam
            for (Exam exam : exams) {
                exam.setNumQuestions(exam.getQuestions().size());
                examRepository.save(exam);
            }
        };
    }


    // =====================================================
    // STEP
    // =====================================================
    @Bean(name = "examStep")
    public Step examStep(
            @Qualifier("examReader")
            MultiResourceItemReader<Map<String, String>> reader,
            @Qualifier("examProcessor")
            ItemProcessor<Map<String, String>, Question> processor,
            @Qualifier("examWriter")
            ItemWriter<Question> writer) {

        return new StepBuilder("examStep", jobRepository)
                .<Map<String, String>, Question>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // =====================================================
    // JOB
    // =====================================================
    @Bean(name = "importExamJob")
    public Job importExamJob(
            @Qualifier("examStep") Step step) {

        return new JobBuilder("importExamJob", jobRepository)
                .start(step)
                .build();
    }

    // =====================================================
    // UTILS
    // =====================================================
    private int parseIntSafe(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) return defaultValue;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String toJson(String raw) {
        try {
            if (raw == null || raw.isBlank()) return "null";
            return objectMapper.writeValueAsString(objectMapper.readTree(raw));
        } catch (Exception e) {
            try {
                return objectMapper.writeValueAsString(raw);
            } catch (Exception ex) {
                return "null";
            }
        }
    }
}
