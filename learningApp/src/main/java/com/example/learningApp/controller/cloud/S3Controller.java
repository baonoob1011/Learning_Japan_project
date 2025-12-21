package com.example.learningApp.controller.cloud;

import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.service.cloud.S3Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3Controller {

    S3Service s3Service;

    // Upload image/audio/video
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type // "images", "audios", "videos"
    ) {
        try {
            String url = s3Service.uploadFile(file, type);
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", url));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not upload file: " + e.getMessage()));
        }
    }

    // Upload CSV file
    @PostMapping("/upload/csv")
    public ResponseEntity<ApiResponse<String>> uploadCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "csv") String folder
    ) {
        try {
            String fileUrl = s3Service.uploadCsvFile(file, folder);
            return ResponseEntity.ok(ApiResponse.success("CSV uploaded successfully", fileUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not upload CSV: " + e.getMessage()));
        }
    }

    @GetMapping("/url")
    public ResponseEntity<ApiResponse<String>> getFileUrl(@RequestParam String key) {
        try {
            String url = s3Service.generatePresignedUrl(key, 3600);
            return ResponseEntity.ok(ApiResponse.success("Pre-signed URL generated", url));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not generate URL: " + e.getMessage()));
        }
    }

    @GetMapping("/keys")
    public ResponseEntity<ApiResponse<List<String>>> getAllS3Keys(
            @RequestParam(required = false) String prefix // images / audios / videos
    ) {
        List<String> keys = s3Service.listAllKeys(prefix);
        return ResponseEntity.ok(
                ApiResponse.success("Get S3 keys successfully", keys)
        );
    }
    @GetMapping("/media/urls")
    public ResponseEntity<ApiResponse<List<String>>> getAllMediaUrls() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get all video & audio URLs successfully",
                        s3Service.getAllMediaUrls()
                )
        );
    }

}
