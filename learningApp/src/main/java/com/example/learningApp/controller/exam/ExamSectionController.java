package com.example.learningApp.controller.exam;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.service.exam.ExamService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.service.exam.ExamSectionService;
import com.example.learningApp.service.exam.ExamService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamSectionController {

    ExamSectionService sectionService;

    @GetMapping("/exam/{examId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByExam(@PathVariable String examId) {
        List<SectionResponse> res = sectionService.getAllSections(examId);
        return ResponseEntity.ok(ApiResponse.success("Sections retrieved", res));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@RequestBody @Valid CreateSectionRequest request) {
        SectionResponse res = sectionService.createSection(request);
        return ResponseEntity.ok(ApiResponse.success("Section created", res));
    }

}

