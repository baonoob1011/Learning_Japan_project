package com.example.learningApp.controller.vocab;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.vocab.VocabStatusResponse;
import com.example.learningApp.service.vocab.VocabStatusService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vocab")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabStatusController {

    VocabStatusService vocabStatusService;

    @GetMapping("/{vocabId}/status")
    public ResponseEntity<ApiResponse<VocabStatusResponse>> getVocabStatus(
            @PathVariable String vocabId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get vocab learning status successfully",
                        vocabStatusService.getStatus(vocabId)
                )
        );
    }
}
