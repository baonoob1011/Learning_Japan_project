package com.example.learningApp.service.cloud;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        // Upload file mà không dùng ACL
        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
        // Lấy URL public dựa vào bucket policy
        return "https://" + bucketName + ".s3." + amazonS3.getRegionName() + ".amazonaws.com/" + fileName;
    }
    public String uploadCsvFile(MultipartFile file, String folder) throws IOException {
        if (!"text/csv".equals(file.getContentType()) && !file.getOriginalFilename().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are allowed");
        }

        String fileName = folder + "/csv_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType("text/csv");

        amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);

        return "https://" + bucketName + ".s3." + amazonS3.getRegionName() + ".amazonaws.com/" + fileName;
    }

}
