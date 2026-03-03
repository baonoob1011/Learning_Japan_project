package com.example.learningApp.controller.exam;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.question.UpdateQuestionRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.service.exam.QuestionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionController {

    QuestionService questionService;

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestions(@PathVariable String sectionId) {
        List<QuestionResponse> res = questionService.getQuestionsBySection(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved", res));
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsByExamId(@PathVariable String examId) {
        List<QuestionResponse> res = questionService.getQuestionsByExamId(examId);
        return ResponseEntity.ok(ApiResponse.success("Questions by exam retrieved successfully", res));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(@RequestBody @Valid CreateQuestionRequest request) {
        QuestionResponse res = questionService.createQuestion(request);
        return ResponseEntity.ok(ApiResponse.success("Question created", res));
    }
    @GetMapping
    public ApiResponse<List<QuestionResponse>> getAll() {
        return ApiResponse.success(questionService.getAll());
    }
    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable String questionId,
            @RequestBody @Valid UpdateQuestionRequest request
    ) {
        QuestionResponse res = questionService.updateQuestion(questionId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Question updated successfully", res)
        );
    }

}


