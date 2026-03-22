package com.example.learningApp.controller.vocab;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.vocab.VocabResponse;
import com.example.learningApp.service.vocab.VocabService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/vocab")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminVocabController {

    VocabService vocabService;

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<PageResponse<VocabResponse>>> getAllVocabsManager(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách từ vựng thành công",
                        vocabService.getAllVocabsManager(page, size, search)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VocabResponse>> createVocab(@RequestBody VocabResponse request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tạo từ vựng thành công",
                        vocabService.adminCreateVocab(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabResponse>> updateVocab(
            @PathVariable String id,
            @RequestBody VocabResponse request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật từ vựng thành công",
                        vocabService.adminUpdateVocab(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVocab(@PathVariable String id) {
        vocabService.adminDeleteVocab(id);
        return ResponseEntity.ok(
                ApiResponse.success("Xóa từ vựng thành công", null));
    }
}
