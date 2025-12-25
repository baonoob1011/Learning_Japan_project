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
import org.springframework.batch.core.*;
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
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
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

    // ================== READER ==================
    @Bean(name = "examReader")
    @StepScope
    public MultiResourceItemReader<Map<String, String>> examReader(
            @Value("#{jobParameters['exam']}") String examKey
    ) {
        MultiResourceItemReader<Map<String, String>> reader = new MultiResourceItemReader<>();
        reader.setResources(loadExamFilesFromS3(examKey));
        reader.setDelegate(singleExamCsvReader());
        return reader;
    }

    @Bean
    public FlatFileItemReader<Map<String, String>> singleExamCsvReader() {
        FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setEncoding("UTF-8");

        String[] names = {
                "exam_code","exam_level","exam_duration",
                "section_title","section_order",
                "question_type","question_text",
                "options","answer","explanation",
                "image_url","audio_url","question_order"
        };

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new LineTokenizer() {
                @Override
                public FieldSet tokenize(String line) {
                    List<String> tokens = new ArrayList<>();
                    boolean inBrackets = false;
                    StringBuilder sb = new StringBuilder();

                    for (char c : line.toCharArray()) {
                        if (c == '[') inBrackets = true;
                        if (c == ']') inBrackets = false;

                        if (c == ',' && !inBrackets) {
                            tokens.add(sb.toString().trim());
                            sb.setLength(0);
                        } else {
                            sb.append(c);
                        }
                    }
                    tokens.add(sb.toString().trim());

                    while (tokens.size() < names.length) {
                        tokens.add("");
                    }

                    int optionsIndex = Arrays.asList(names).indexOf("options");
                    if (optionsIndex >= 0) {
                        String opt = tokens.get(optionsIndex);
                        if (opt.startsWith("[") && opt.endsWith("]")) {
                            opt = opt.substring(1, opt.length() - 1).trim();
                            tokens.set(optionsIndex, opt);
                        }
                    }

                    return new DefaultFieldSet(tokens.toArray(new String[0]), names);
                }
            });

            setFieldSetMapper(fs -> {
                Map<String, String> map = new HashMap<>();
                for (String name : names) {
                    map.put(name, fs.readString(name));
                }
                map.put("options", parseOptions(map.get("options")));
                return map;
            });
        }});

        return reader;
    }

    private Resource[] loadExamFilesFromS3(String examKey) {
        List<Resource> resources = new ArrayList<>();
        List<S3ObjectSummary> objects = amazonS3.listObjects(bucketName, "exam/").getObjectSummaries();

        for (S3ObjectSummary obj : objects) {
            if (!obj.getKey().endsWith(".csv")) continue;
            if (examKey != null && !obj.getKey().contains(examKey)) continue;
            try {
                resources.add(new UrlResource(amazonS3.getUrl(bucketName, obj.getKey())));
            } catch (Exception e) {
                throw new RuntimeException("Cannot load exam file: " + obj.getKey(), e);
            }
        }

        if (resources.isEmpty()) throw new IllegalStateException("No CSV files found in S3 folder exam/");
        return resources.toArray(new Resource[0]);
    }

    // ================== PROCESSOR ==================
    @Bean(name = "examProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, Question> examProcessor() {
        Map<String, Exam> examCache = new HashMap<>();
        Map<String, ExamSection> sectionCache = new HashMap<>();

        return row -> {
            if (row == null || row.get("question_type").isBlank() || row.get("question_text").isBlank()) return null;

            try {
                String examCode = Optional.ofNullable(row.get("exam_code")).orElse("").trim();
                String examLevel = Optional.ofNullable(row.get("exam_level")).orElse("").trim();
                int examDuration = parseIntSafe(row.get("exam_duration"), 0);
                String sectionTitle = Optional.ofNullable(row.get("section_title")).orElse("").trim();
                int sectionOrder = parseIntSafe(row.get("section_order"), 0);
                int questionOrder = parseIntSafe(row.get("question_order"), 0);
                String questionTypeStr = Optional.ofNullable(row.get("question_type")).orElse("").trim();

                Exam exam = examCache.computeIfAbsent(examCode, code ->
                        examRepository.findByCode(code)
                                .orElseGet(() -> examRepository.save(
                                        Exam.builder()
                                                .code(code)
                                                .level(examLevel)
                                                .duration(examDuration)
                                                .sections(new ArrayList<>())
                                                .questions(new ArrayList<>())
                                                .numQuestions(0)
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build()
                                ))
                );

                String sectionKey = sectionTitle + "_" + sectionOrder + "_" + examLevel;
                ExamSection section = sectionCache.computeIfAbsent(sectionKey, key ->
                        sectionRepository.findByTitleAndSectionOrderAndLevel(sectionTitle, sectionOrder, examLevel)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Section not found: title=" + sectionTitle
                                                + ", order=" + sectionOrder
                                                + ", level=" + examLevel
                                ))
                );


                // ⚡ Nối Exam <-> Section
                if (!exam.getSections().contains(section)) {
                    exam.getSections().add(section);
                    // Cập nhật tổng số section mỗi khi thêm mới
                    exam.setNumSections(exam.getSections().size());
                }

                if (!section.getExams().contains(exam)) {
                    section.getExams().add(exam);
                }
                if (!section.getExams().contains(exam)) {
                    section.getExams().add(exam);
                }

                AssessmentType questionType;
                try { questionType = AssessmentType.valueOf(questionTypeStr); }
                catch (IllegalArgumentException e) { return null; }

                String answer = Optional.ofNullable(row.get("answer")).orElse("").trim();
                String optionsParsed = parseOptions(row.get("options"));

                Question question = Question.builder()
                        .section(section)
                        .questionType(questionType)
                        .questionText(row.get("question_text").trim())
                        .options(optionsParsed)
                        .answer(answer)
                        .explanation(Optional.ofNullable(row.get("explanation")).orElse("").trim())
                        .imageUrl(row.get("image_url"))
                        .audioUrl(row.get("audio_url"))
                        .questionOrder(questionOrder)
                        .build();

                question.getExams().add(exam);
                exam.getQuestions().add(question);

                return question;

            } catch (Exception e) {
                return null;
            }
        };
    }

    // ================== WRITER ==================
    @Bean(name = "examWriter")
    public ItemWriter<Question> examWriter() {
        return items -> {
            List<Question> list = StreamSupport.stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            questionRepository.saveAll(list);

            Set<Exam> exams = list.stream()
                    .flatMap(q -> q.getExams().stream())
                    .collect(Collectors.toSet());

            for (Exam exam : exams) {
                exam.setNumQuestions(exam.getQuestions().size());
                examRepository.save(exam);
            }
        };
    }

    @Bean(name = "delayedExamProcessor")
    @StepScope
    public ItemProcessor<Map<String, String>, Question> delayedExamProcessor(
            @Qualifier("examProcessor") ItemProcessor<Map<String, String>, Question> delegate
    ) {
        return item -> {
            if (item != null) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            return delegate.process(item);
        };
    }

    // ================== STEP & JOB ==================
    @Bean(name = "examStep")
    public Step examStep(
            @Qualifier("examReader") MultiResourceItemReader<Map<String, String>> reader,
            @Qualifier("delayedExamProcessor") ItemProcessor<Map<String, String>, Question> processor,
            @Qualifier("examWriter") ItemWriter<Question> writer
    ) {
        return new StepBuilder("examStep", jobRepository)
                .<Map<String, String>, Question>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importExamJob(
            @Qualifier("examStep") Step examStep
    ) {
        return new JobBuilder("importExamJob", jobRepository)
                .start(examStep)
                .build();
    }

    // ================== UTILS ==================
    private int parseIntSafe(String value, int defaultValue) {
        try { return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value.trim()); }
        catch (Exception e) { return defaultValue; }
    }

    private String parseOptions(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("[]")) return "[]";

        raw = raw.trim();
        if (raw.startsWith("[") && raw.endsWith("]")) {
            raw = raw.substring(1, raw.length() - 1);
        }

        List<String> list = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceAll("^\"|\"$", ""))
                .map(s -> s.replaceAll("^'|'$", ""))
                .toList();

        try { return objectMapper.writeValueAsString(list); }
        catch (Exception e) { return "[]"; }
    }
}
