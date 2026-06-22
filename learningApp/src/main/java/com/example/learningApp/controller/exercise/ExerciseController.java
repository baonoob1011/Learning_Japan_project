package com.example.learningApp.controller.exercise;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.excercise.GenerateExerciseRequest;
import com.example.learningApp.dto.response.excercise.ExerciseDetailResponse;
import com.example.learningApp.dto.response.excercise.ExerciseResponse;
import com.example.learningApp.dto.response.excercise.QuestionResponse;
import com.example.learningApp.service.video.exercise.ExerciseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-exercises")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExerciseController {

    ExerciseService exerciseService;

    /* ===================== GENERATE ===================== */

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ExerciseResponse>> generateExercise(
            @RequestBody GenerateExerciseRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Generate exercise successfully",
                        exerciseService.generateFromVideo(request)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{exerciseId}")
    public ResponseEntity<ApiResponse<ExerciseDetailResponse>> getExercise(
            @PathVariable String exerciseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get exercise successfully",
                        exerciseService.getExercise(exerciseId)
                )
        );
    }

    /* ===================== GET QUESTIONS ===================== */

    @GetMapping("/{exerciseId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestions(
            @PathVariable String exerciseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get questions successfully",
                        exerciseService.getQuestionsByExercise(exerciseId)
                )
        );
    }

    /* ===================== GET BY VIDEO ===================== */

    @GetMapping("/video/{videoId}")
    public ResponseEntity<ApiResponse<ExerciseDetailResponse>> getExerciseByVideoId(
            @PathVariable String videoId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get exercise by video successfully",
                        exerciseService.getExerciseWithQuestionsByVideoId(videoId)
                )
        );
    }
}
