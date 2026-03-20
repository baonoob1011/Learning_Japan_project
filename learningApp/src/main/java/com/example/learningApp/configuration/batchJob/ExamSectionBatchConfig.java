package com.example.learningApp.configuration.batchJob;

import com.example.learningApp.entity.AssessmentItem;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ExamSectionBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "examSectionStep")
    public Step examSectionStep(
            @Qualifier("examSectionReader") MultiResourceItemReader<Map<String, String>> reader,
            @Qualifier("examSectionProcessor") ItemProcessor<Map<String, String>, AssessmentItem> processor,
            @Qualifier("examSectionWriter") ItemWriter<AssessmentItem> writer) {

        return new StepBuilder("examSectionStep", jobRepository)
                .<Map<String, String>, AssessmentItem>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean(name = "importExamSectionJob")
    public Job importExamSectionJob(
            @Qualifier("examSectionStep") Step step,
            @Qualifier("examSectionJobListener") JobExecutionListener listener) {

        return new JobBuilder("importExamSectionJob", jobRepository)
                .listener(listener)
                .start(step)
                .build();
    }
}

