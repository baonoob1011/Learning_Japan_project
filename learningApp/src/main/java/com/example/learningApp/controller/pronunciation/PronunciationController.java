package com.example.learningApp.controller.pronunciation;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.pronunciation.PronunciationResultResponse;
import com.example.learningApp.service.pronunciation.PronunciationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pronunciation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER_VIP')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PronunciationController {

    PronunciationService service;

    /**
     * 1️⃣ Submit audio → nhận jobId ngay
     */
    @PostMapping(
            value = "/submit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<String>> submitPronunciation(
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart("expectedText") String expectedText
    ) {
        // Pass expectedText xuống service
        String jobId = service.submitPronunciation(audioFile, expectedText);
        return ResponseEntity.ok(
                ApiResponse.success("Pronunciation job submitted", jobId)
        );
    }

    /**
     * 2️⃣ Get result by jobId
     */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<PronunciationResultResponse>> getResult(
            @RequestParam("jobId") String jobId
    ) {
        PronunciationResultResponse result = service.getResult(jobId); // trả null nếu chưa xong
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.success("Processing", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Pronunciation result", result));
    }
}

