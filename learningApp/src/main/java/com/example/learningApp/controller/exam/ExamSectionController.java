package com.example.learningApp.controller.exam;

import com.example.learningApp.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.service.exam.ExamSectionService;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamSectionController {

    ExamSectionService sectionService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody @Valid CreateSectionRequest request) {
        SectionResponse res = sectionService.createSection(request);
        return ResponseEntity.ok(ApiResponse.success("Section created", res));
    }

}

