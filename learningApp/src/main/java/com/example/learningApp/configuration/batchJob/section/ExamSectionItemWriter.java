package com.example.learningApp.configuration.batchJob.section;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.example.learningApp.entity.AssessmentItem;
import com.example.learningApp.repository.AssessmentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ExamSectionItemWriter {

    private final AssessmentItemRepository assessmentItemRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean(name = "examSectionWriter")
    public ItemWriter<AssessmentItem> examSectionWriter() {
        return items -> {
            List<AssessmentItem> assessmentItems = StreamSupport
                    .stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            assessmentItemRepository.saveAll(assessmentItems);
        };
    }

    /**
     * Sau khi Batch Job hoàn tất thành công → move tất cả file CSV
     * từ assessment/ sang assessment/processed/ trên S3
     */
    @Bean(name = "examSectionJobListener")
    public JobExecutionListener examSectionJobListener() {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    moveProcessedFilesToArchive();
                }
            }
        };
    }

    private void moveProcessedFilesToArchive() {
        try {
            List<com.amazonaws.services.s3.model.S3ObjectSummary> objects = amazonS3
                    .listObjects(bucketName, "assessment/").getObjectSummaries();

            for (com.amazonaws.services.s3.model.S3ObjectSummary obj : objects) {
                String key = obj.getKey();

                // Chỉ move file .csv ở thư mục gốc assessment/ (không move file trong
                // processed/)
                if (key.endsWith(".csv") && !key.contains("/processed/")) {
                    String fileName = key.substring(key.lastIndexOf("/") + 1);
                    String destKey = "assessment/processed/" + fileName;

                    // Copy sang processed/
                    amazonS3.copyObject(new CopyObjectRequest(bucketName, key, bucketName, destKey));
                    // Xóa file gốc
                    amazonS3.deleteObject(bucketName, key);

                    System.out.println("✅ Moved to processed: " + key + " → " + destKey);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to move files to processed folder: " + e.getMessage());
        }
    }
}

