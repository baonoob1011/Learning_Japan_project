package com.example.learningApp.service.cloud;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
            "mp4", "mov", "avi",
            "mp3", "wav", "aac"
    );

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
    public String generatePresignedUrl(String key, int expirationSeconds) {
        Date expiration = new Date();
        expiration.setTime(System.currentTimeMillis() + expirationSeconds * 1000L);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }
    public List<String> listAllKeys(String prefix) {

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix); // null = toàn bucket

        ListObjectsV2Result result = amazonS3.listObjectsV2(request);

        return result.getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }
    public List<String> getAllMediaUrls() {

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName);

        ListObjectsV2Result result = amazonS3.listObjectsV2(request);

        return result.getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .filter(this::isMediaFile)
                .map(this::buildPublicUrl)
                .collect(Collectors.toList());
    }

    private boolean isMediaFile(String key) {
        int lastDot = key.lastIndexOf(".");
        if (lastDot == -1) return false;
        String ext = key.substring(lastDot + 1).toLowerCase();
        return MEDIA_EXTENSIONS.contains(ext);
    }

    private String buildPublicUrl(String key) {
        return "https://" + bucketName + ".s3."
                + amazonS3.getRegionName()
                + ".amazonaws.com/" + key;
    }
}
