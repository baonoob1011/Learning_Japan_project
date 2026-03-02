package com.example.learningApp.controller.assessment;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.exam.assessment.UpdateAssessmentItemRequest;
import com.example.learningApp.dto.response.exam.assessment.AssessmentItemResponse;
import com.example.learningApp.service.assessment.AssessmentItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assessment-items")
@RequiredArgsConstructor
public class AssessmentItemController {

    private final AssessmentItemService service;

    /* ===================== GET BY SECTION ===================== */

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<AssessmentItemResponse>>> getBySection(
            @PathVariable String sectionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get assessment items successfully",
                        service.getBySection(sectionId)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<AssessmentItemResponse>> getDetail(
            @PathVariable String itemId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get assessment item detail successfully",
                        service.getDetail(itemId)
                )
        );
    }

    /* ===================== UPDATE ===================== */

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable String itemId,
            @RequestBody UpdateAssessmentItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        service.update(itemId, request),
                        null
                )
        );
    }
}