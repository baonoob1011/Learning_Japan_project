package com.example.learningApp.controller.cloud;

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

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

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
}
