package com.example.learningApp.controller.kanji;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.kanji.CreateKanjiRequest;
import com.example.learningApp.dto.request.kanji.KanjiStrokeRequest;
import com.example.learningApp.dto.request.kanji.UpdateKanjiRequest;
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

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<KanjiResponse>> getKanji(
                        @PathVariable String id) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Kanji fetched successfully",
                                                kanjiService.getKanjiById(id)));
        }

        @PostMapping
        public ResponseEntity<ApiResponse<KanjiResponse>> createKanji(
                        @RequestBody CreateKanjiRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Kanji created successfully",
                                                kanjiService.createKanji(request)));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<KanjiResponse>>> getAllKanji() {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Kanji list fetched successfully",
                                                kanjiService.getAllKanji()));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<KanjiResponse>> updateKanji(
                        @PathVariable String id,
                        @RequestBody UpdateKanjiRequest request) {
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Kanji updated successfully",
                                                kanjiService.updateKanji(id, request)));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteKanji(
                        @PathVariable String id) {
                kanjiService.deleteKanji(id);
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Kanji deleted successfully",
                                                null));
        }
}
