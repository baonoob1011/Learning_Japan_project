package com.example.learningApp.controller.translate;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.translate.TranslateRequest;
import com.example.learningApp.dto.response.translate.TranslateResponse;
import com.example.learningApp.service.translate.TranslateService;
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

    private final TranslateService translateService;

    @PostMapping
    public ResponseEntity<ApiResponse<TranslateResponse>> translate(
            @RequestBody @Valid TranslateRequest request
    ) throws IOException, InterruptedException {
        TranslateResponse result = translateService.translate(
                request.getText(),
                request.getSourceLang(),
                request.getTargetLang()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Translate successfully", result)
        );
    }
}
