package com.example.learningApp.configuration.batchJob;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.enums.AssessmentType;
import com.example.learningApp.repository.ExamSectionRepository;
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
public class ExamSectionBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExamSectionRepository sectionRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Reader chính: đọc NHIỀU FILE từ S3
     */
    @Bean(name = "examSectionReader")
    @StepScope
    public MultiResourceItemReader<Map<String, String>> examSectionReader() {

        MultiResourceItemReader<Map<String, String>> reader =
                new MultiResourceItemReader<>();

        reader.setResources(loadAssessmentFilesFromS3());
        reader.setDelegate(singleCsvReader());

        return reader;
    }

    /**
     * Reader đọc 1 file CSV
     */
    @Bean
    public FlatFileItemReader<Map<String, String>> singleCsvReader() {

        FlatFileItemReader<Map<String, String>> reader =
                new FlatFileItemReader<>();

        reader.setLinesToSkip(1);
        reader.setEncoding("UTF-8");

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(",");
                setStrict(false);
                setNames(
                        "section_title",
                        "section_order",
                        "assessment_type",
                        "assessment_name",
                        "assessment_level",
                        "question_count",
                        "point_per_question"
                );
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


    /**
     * Load tất cả file assessment/*.csv từ S3
     */
    private Resource[] loadAssessmentFilesFromS3() {

        List<Resource> resources = new ArrayList<>();

        List<S3ObjectSummary> objects =
                amazonS3.listObjects(bucketName, "assessment/")
                        .getObjectSummaries();

        for (S3ObjectSummary obj : objects) {
            if (obj.getKey().endsWith(".csv")) {
                try {
                    String url = amazonS3
                            .getUrl(bucketName, obj.getKey())
                            .toString();

                    System.out.println("📥 Importing file: " + obj.getKey());
                    resources.add(new UrlResource(url));

                } catch (Exception e) {
                    throw new RuntimeException("Cannot load S3 file: " + obj.getKey(), e);
                }
            }
        }

        if (resources.isEmpty()) {
            throw new IllegalStateException(
                    "❌ Không tìm thấy file CSV trong S3 folder assessment/");
        }

        return resources.toArray(new Resource[0]);
    }


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
                int sectionOrder = parseIntSafe(row.get("section_order"), 0);
                String sectionLevel = row.get("assessment_level").trim();

                String sectionKey =
                        sectionTitle + "_" + sectionOrder + "_" + sectionLevel;

                // ================== LOAD / CREATE SECTION ==================
                ExamSection section = sectionCache.computeIfAbsent(sectionKey, key ->
                        sectionRepository
                                .findByTitleAndOrderNumAndLevel(
                                        sectionTitle,
                                        sectionOrder,
                                        sectionLevel
                                )
                                .orElseGet(() -> ExamSection.builder()
                                        .title(sectionTitle)
                                        .orderNum(sectionOrder)
                                        .level(sectionLevel)
                                        .assessmentItems(new ArrayList<>())
                                        .questions(new ArrayList<>())
                                        .build())
                );

                // ================== PARSE ENUM SAFE ==================
                AssessmentType type = AssessmentType.valueOf(
                        row.get("assessment_type").trim()
                );

                int questionCount =
                        parseIntSafe(row.get("question_count"), 0);
                float pointPerQuestion =
                        parseFloatSafe(row.get("point_per_question"), 0f);

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



    @Bean(name = "examSectionWriter")
    public ItemWriter<ExamSection> examSectionWriter() {
        return items -> {
            List<ExamSection> sections = StreamSupport
                    .stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            sectionRepository.saveAll(sections);
        };
    }


    @Bean(name = "examSectionStep")
    public Step examSectionStep(
            @Qualifier("examSectionReader")
            MultiResourceItemReader<Map<String, String>> reader,
            @Qualifier("examSectionProcessor")
            ItemProcessor<Map<String, String>, ExamSection> processor,
            @Qualifier("examSectionWriter")
            ItemWriter<ExamSection> writer) {

        return new StepBuilder("examSectionStep", jobRepository)
                .<Map<String, String>, ExamSection>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }



    @Bean(name = "importExamSectionJob")
    public Job importExamSectionJob(
            @Qualifier("examSectionStep") Step step) {

        return new JobBuilder("importExamSectionJob", jobRepository)
                .start(step)
                .build();
    }


    private int parseIntSafe(String value, int defaultValue) {
        try {
            if (value == null || value.isBlank()) return defaultValue;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float parseFloatSafe(String value, float defaultValue) {
        try {
            if (value == null || value.isBlank()) return defaultValue;
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
