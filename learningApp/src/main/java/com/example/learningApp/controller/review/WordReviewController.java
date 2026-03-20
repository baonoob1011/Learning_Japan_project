package com.example.learningApp.controller.review;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.review.AddWordRequest;
import com.example.learningApp.dto.response.review.ReviewWordItemResponse;
import com.example.learningApp.service.review.ReviewSessionService;
import com.example.learningApp.service.vocab.VocabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/words")
@RequiredArgsConstructor
public class WordReviewController {

    private final VocabService vocabService;
    private final ReviewSessionService reviewSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addWord(@Valid @RequestBody AddWordRequest request) {
        vocabService.saveVocabForCurrentUser(request.getSurface());
        return ResponseEntity.ok(ApiResponse.success("Word added to your learning list", null));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<ReviewWordItemResponse>>> getOverdueWords(
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success("Fetched overdue words", reviewSessionService.getOverdueWordsForCurrentUser(limit)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Fetched word stats", reviewSessionService.getWordStatsForCurrentUser()));
    }
}

