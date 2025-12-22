package com.example.learningApp.controller.batch;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.enums.BatchJobType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BatchJobController {

    JobLauncher jobLauncher;

    @Qualifier("importExamJob")
    Job importExamJob;

    @Qualifier("importExamSectionJob")
    Job importExamSectionJob;

    /**
     * ================= RUN BATCH JOB =================
     *
     * jobType = EXAM
     *  - đọc exam/*.csv từ S3
     *
     * jobType = SECTION_ASSESSMENT
     *  - đọc assessment/*.csv từ S3
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<String>> runBatchJob(
            @RequestParam("jobType") BatchJobType jobType
    ) {
        try {
            Job jobToRun;

            switch (jobType) {
                case EXAM -> jobToRun = importExamJob;
                case SECTION_ASSESSMENT -> jobToRun = importExamSectionJob;
                default -> throw new IllegalArgumentException(
                        "Unsupported job type: " + jobType
                );
            }

            JobParameters jobParameters =
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis()) // bắt buộc để run lại
                            .toJobParameters();

            JobExecution execution =
                    jobLauncher.run(jobToRun, jobParameters);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Batch job started successfully",
                            "jobType=" + jobType +
                                    ", jobExecutionId=" + execution.getId()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(
                            500,
                            "Failed to run batch job: " + e.getMessage()
                    ));
        }
    }
}
