package com.example.learningApp.configuration.batchJob;

import com.example.learningApp.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ExamBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean(name = "examStep")
    public Step examStep(
            @Qualifier("examReader") MultiResourceItemReader<Map<String, String>> reader,
            @Qualifier("delayedExamProcessor") ItemProcessor<Map<String, String>, Question> processor,
            @Qualifier("examWriter") ItemWriter<Question> writer) {
        return new StepBuilder("examStep", jobRepository)
                .<Map<String, String>, Question>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
                            String examKey = stepExecution.getJobParameters().getString("exam");
                            List<S3ObjectSummary> objects = amazonS3.listObjects(bucketName, "exam/")
                                    .getObjectSummaries();
                            for (S3ObjectSummary obj : objects) {
                                if (obj.getKey().endsWith(".csv")
                                        && (examKey == null || obj.getKey().contains(examKey))) {
                                    String newKey = obj.getKey().replaceFirst("^exam/", "exam_processed/");
                                    amazonS3.copyObject(bucketName, obj.getKey(), bucketName, newKey);
                                    amazonS3.deleteObject(bucketName, obj.getKey());
                                }
                            }
                        }
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job importExamJob(
            @Qualifier("examStep") Step examStep) {
        return new JobBuilder("importExamJob", jobRepository)
                .start(examStep)
                .build();
    }
}

