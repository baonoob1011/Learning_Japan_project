package com.example.learningApp.service.pronunciation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-nam}")
    private String bucket;

    public String upload(File file) {

        String key = "pronunciation/practice_" + System.currentTimeMillis() + ".wav";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("audio/wav")
                        .build(),
                file.toPath()
        );

        log.info("✅ Uploaded to S3: {}", key);
        return key;
    }

    public void delete(String key) {

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        log.info("🗑 Deleted from S3: {}", key);
    }
}