package com.example.learningApp.controller;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.service.cloud.S3Service;
import com.example.learningApp.service.vocab.VocabService;
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
@RequestMapping("/api/v1/vocab")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabController {

    VocabService vocabService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveVocab(@RequestBody CreateVocabRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Saved vocab successfully  ",  vocabService.saveVocabForCurrentUser(request.getSurface())));
    }

}
