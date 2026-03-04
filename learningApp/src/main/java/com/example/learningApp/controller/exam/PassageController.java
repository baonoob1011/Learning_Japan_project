package com.example.learningApp.controller.exam;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.exam.UpdatePassageRequest;
import com.example.learningApp.dto.response.exam.PassageResponse;
import com.example.learningApp.service.exam.PassageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/passages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class PassageController {

    PassageService passageService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PassageResponse>> updatePassage(
            @PathVariable String id,
            @RequestBody @Valid UpdatePassageRequest request
    ) {
        PassageResponse response = passageService.updatePassage(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Passage updated successfully", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PassageResponse>> getPassage(
            @PathVariable String id
    ) {
        PassageResponse response = passageService.getPassageById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Passage retrieved successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePassage(
            @PathVariable String id
    ) {
        passageService.deletePassage(id);
        return ResponseEntity.ok(
                ApiResponse.success("Passage deleted successfully", null)
        );
    }
}

