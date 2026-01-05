package com.example.learningApp.controller.vocab;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.vocab.CreateVocabRequest;
import com.example.learningApp.dto.request.vocab.UpdateVocabRequest;
import com.example.learningApp.dto.response.vocab.VocabResponse;
import com.example.learningApp.service.vocab.VocabService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    public ResponseEntity<ApiResponse<List<VocabResponse>>> getMyVocabs() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get saved vocabs successfully",
                        vocabService.getSavedVocabsOfCurrentUser()
                )
        );
    }
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateVocabMeaning(
            @RequestBody UpdateVocabRequest request
    ) {
        vocabService.updateVocabMeaning(request);
        return ResponseEntity.ok(
                ApiResponse.success("Updated vocab meaning successfully", null)
        );
    }

    /**
     * ✅ Xóa vocab khỏi danh sách đã lưu của user hiện tại
     */
    @DeleteMapping("/{surface}")
    public ResponseEntity<ApiResponse<Void>> removeVocab(
            @PathVariable String surface
    ) {
        vocabService.removeVocabForCurrentUser(surface);
        return ResponseEntity.ok(
                ApiResponse.success("Removed vocab successfully", null)
        );
    }
}
