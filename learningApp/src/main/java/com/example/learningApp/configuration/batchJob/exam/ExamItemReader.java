package com.example.learningApp.configuration.batchJob.exam;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.learningApp.configuration.batchJob.BatchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ExamItemReader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean(name = "examReader")
    @StepScope
    public MultiResourceItemReader<Map<String, String>> examReader(
            @Value("#{jobParameters['exam']}") String examKey) {
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
                "exam_code", "exam_level", "exam_duration",
                "section_title", "section_order",
                "question_type", "question_text",
                "options", "answer", "explanation",
                "image_url", "audio_url", "question_order"
        };

        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                setLineTokenizer(new LineTokenizer() {
                    @Override
                    public FieldSet tokenize(String line) {
                        List<String> tokens = new ArrayList<>();
                        boolean inBrackets = false;
                        StringBuilder sb = new StringBuilder();

                        for (char c : line.toCharArray()) {
                            if (c == '[')
                                inBrackets = true;
                            if (c == ']')
                                inBrackets = false;

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
                    map.put("options", BatchUtils.parseOptions(map.get("options")));
                    return map;
                });
            }
        });

        return reader;
    }

    private Resource[] loadExamFilesFromS3(String examKey) {
        List<Resource> resources = new ArrayList<>();
        List<S3ObjectSummary> objects = amazonS3.listObjects(bucketName, "exam/").getObjectSummaries();

        for (S3ObjectSummary obj : objects) {
            if (!obj.getKey().endsWith(".csv"))
                continue;
            if (examKey != null && !obj.getKey().contains(examKey))
                continue;
            try {
                resources.add(new UrlResource(amazonS3.getUrl(bucketName, obj.getKey())));
            } catch (Exception e) {
                throw new RuntimeException("Cannot load exam file: " + obj.getKey(), e);
            }
        }

        if (resources.isEmpty())
            throw new IllegalStateException("No CSV files found in S3 folder exam/");
        return resources.toArray(new Resource[0]);
    }
}
