package com.example.learningApp.configuration.batchJob.section;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExamSectionItemReader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean(name = "examSectionReader")
    @StepScope
    public MultiResourceItemReader<Map<String, String>> examSectionReader() {
        MultiResourceItemReader<Map<String, String>> reader = new MultiResourceItemReader<>();
        reader.setResources(loadAssessmentFilesFromS3());
        reader.setDelegate(singleCsvReader());
        return reader;
    }

    @Bean
    public FlatFileItemReader<Map<String, String>> singleCsvReader() {
        FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setEncoding("UTF-8");

        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setDelimiter(",");
                        setStrict(false);
                        setNames(
                                "section_title",
                                "section_order",
                                "section_duration",
                                "assessment_type",
                                "assessment_name",
                                "assessment_level",
                                "question_count",
                                "point_per_question");
                    }
                });
                setFieldSetMapper(fieldSet -> {
                    Map<String, String> map = new HashMap<>();
                    for (String name : fieldSet.getNames()) {
                        map.put(name, fieldSet.readString(name));
                    }
                    return map;
                });
            }
        });

        return reader;
    }

    private Resource[] loadAssessmentFilesFromS3() {
        List<Resource> resources = new ArrayList<>();
        List<S3ObjectSummary> objects = amazonS3.listObjects(bucketName, "assessment/").getObjectSummaries();

        for (S3ObjectSummary obj : objects) {
            if (obj.getKey().endsWith(".csv")) {
                try {
                    String url = amazonS3.getUrl(bucketName, obj.getKey()).toString();
                    System.out.println("📥 Importing file: " + obj.getKey());
                    resources.add(new UrlResource(url));
                } catch (Exception e) {
                    throw new RuntimeException("Cannot load S3 file: " + obj.getKey(), e);
                }
            }
        }

        if (resources.isEmpty()) {
            throw new IllegalStateException("❌ Không tìm thấy file CSV trong S3 folder assessment/");
        }

        return resources.toArray(new Resource[0]);
    }
}
