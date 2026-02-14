package com.example.learningApp.controller.kanji;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.request.kanji.KanjiStrokeRequest;
import com.example.learningApp.dto.response.kanji.KanjiCheckResponse;
import com.example.learningApp.dto.response.kanji.KanjiResponse;
import com.example.learningApp.service.kanji.KanjiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kanji")
@RequiredArgsConstructor
public class KanjiController {

    private final KanjiService kanjiService;

    // =================================================
    // 1️⃣ Get Kanji By ID
    // =================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KanjiResponse>> getKanji(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kanji fetched successfully",
                        kanjiService.getKanjiById(id)
                )
        );
    }

    // =================================================
    // 2️⃣ Check Kanji Writing
    // =================================================

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<KanjiCheckResponse>> checkKanji(
            @AuthenticationPrincipal(expression = "id") String userId,
            @RequestBody KanjiStrokeRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kanji checked successfully",
                        kanjiService.checkKanji(userId, request)
                )
        );
    }

    // =================================================
    // GET ALL
    // =================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<KanjiResponse>>> getAllKanji() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kanji list fetched successfully",
                        kanjiService.getAllKanji()
                )
        );
    }

    // =================================================
    // CREATE
    // =================================================

    @PostMapping
    public ResponseEntity<ApiResponse<KanjiResponse>> createKanji(
            @RequestBody CreateKanjiRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kanji created successfully",
                        kanjiService.createKanji(request)
                )
        );
    }
}
