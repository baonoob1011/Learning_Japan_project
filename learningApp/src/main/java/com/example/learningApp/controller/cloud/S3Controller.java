package com.example.learningApp.controller.cloud;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.S3ImageResponse;
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
            @RequestParam("type") String type
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
    @GetMapping("/images/urls")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<S3ImageResponse>>> getAllImagesUrls() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get all image URLs successfully",
                        s3Service.getAllImagesUrls()
                )
        );
    }

    @DeleteMapping("/image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestParam String key
    ) {
        try {
            s3Service.deleteFile(key);
            return ResponseEntity.ok(
                    ApiResponse.success("Image deleted successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not delete image: " + e.getMessage()));
        }
    }

    @GetMapping("/audios/urls")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<S3ImageResponse>>> getAllAudioUrls() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get all audio URLs successfully",
                        s3Service.getAllAudioUrls()
                )
        );
    }

    @DeleteMapping("/audio")
    public ResponseEntity<ApiResponse<Void>> deleteAudio(
            @RequestParam String key
    ) {
        try {
            s3Service.deleteFile(key);
            return ResponseEntity.ok(
                    ApiResponse.success("Audio deleted successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not delete audio: " + e.getMessage()));
        }
    }

    @GetMapping("/assessment/urls")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<S3ImageResponse>>> getAllAssessmentUrls() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get all assessment URLs successfully",
                        s3Service.getAllAssessmentUrls()
                )
        );
    }

    @DeleteMapping("/assessment")
    public ResponseEntity<ApiResponse<Void>> deleteAssessment(
            @RequestParam String key
    ) {
        try {
            s3Service.deleteFile(key);
            return ResponseEntity.ok(
                    ApiResponse.success("Assessment file deleted successfully", null)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Could not delete assessment file: " + e.getMessage()));
        }
    }

}
