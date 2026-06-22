package com.example.learningApp.controller.course.section;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.course.section.CreateSectionRequest;
import com.example.learningApp.dto.request.course.section.UpdateSectionRequest;
import com.example.learningApp.dto.response.course.section.SectionResponse;
import com.example.learningApp.service.course.section.SectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/section")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SectionController {

    SectionService sectionService;

    /* ===================== CREATE ===================== */
@PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createSection(
            @RequestBody CreateSectionRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create section successfully",
                        sectionService.createSection(request)
                )
        );
    }
    /* ===================== UPDATE ===================== */

    @PutMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<String>> updateSection(
            @PathVariable String sectionId,
            @RequestBody UpdateSectionRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update section successfully",
                        sectionService.updateSection(sectionId, request)
                )
        );
    }
    /* ===================== GET BY COURSE ===================== */

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByCourse(
            @PathVariable String courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get sections successfully",
                        sectionService.getSectionsByCourse(courseId)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionDetail(
            @PathVariable String sectionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get section detail successfully",
                        sectionService.getSectionDetail(sectionId)
                )
        );
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable String sectionId
    ) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.ok(
                ApiResponse.success("Delete section successfully", null)
        );
    }
}

