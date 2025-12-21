package com.example.learningApp.controller.batch;

import com.example.learningApp.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamBatchController {

    Job importExamJob;
    JobLauncher jobLauncher;

    @PostMapping("/trigger-job")
    public ResponseEntity<ApiResponse<String>> triggerJob(@RequestParam("file") MultipartFile file) {
        try {
            // 1️⃣ Lưu file tạm
            File tempFile = File.createTempFile("exam_upload_", ".csv");
            file.transferTo(tempFile);

            // 2️⃣ Tạo JobParameters
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", tempFile.getAbsolutePath())
                    .addLong("time", System.currentTimeMillis()) // đảm bảo duy nhất
                    .toJobParameters();

            // 3️⃣ Trigger batch job
            JobExecution jobExecution = jobLauncher.run(importExamJob, jobParameters);

            // 4️⃣ Trả về theo chuẩn ApiResponse
            String message = "Batch job started with status: " + jobExecution.getStatus();
            return ResponseEntity.ok(ApiResponse.success(message, jobExecution.getId().toString()));

        } catch (Exception e) {
            // Trả về ApiResponse.error có code và message
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Failed to run batch job: " + e.getMessage()));
        }
    }


}
