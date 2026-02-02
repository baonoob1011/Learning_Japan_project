package com.example.learningApp.controller.translate;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.service.vocab.VocabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/translates")
@RequiredArgsConstructor
public class TranslateController {

    private final VocabService vocabService;

    @PostMapping
    public ResponseEntity<ApiResponse<TranslateResponse>> translate(
            @RequestBody @Valid TranslateRequest request
    ) throws IOException, InterruptedException {
        TranslateResponse result = vocabService.findOrTranslate(
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Translate successfully", result)
        );
    }
}
