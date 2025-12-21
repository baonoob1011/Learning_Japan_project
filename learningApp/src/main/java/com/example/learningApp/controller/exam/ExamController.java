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

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ExamController {
    ExamService examService;

    // Create exam
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@RequestBody @Valid CreateExamRequest request) {
        ExamResponse response = examService.createExam(request);
        return ResponseEntity.ok(ApiResponse.success("Exam created successfully", response));
    }

    // Get all exams
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams() {
        List<ExamResponse> response = examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.success("Exams retrieved successfully", response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> searchExams(
            @RequestParam("key") String keyword) {
        List<ExamResponse> response = examService.searchExams(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results", response));
    }

    // Get exam by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable String id) {
        ExamResponse response = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.success("Exam retrieved successfully", response));
    }
}
