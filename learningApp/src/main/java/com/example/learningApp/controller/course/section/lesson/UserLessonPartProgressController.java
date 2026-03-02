package com.example.learningApp.controller.course.section.lesson;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.course.section.lesson.UpdateLessonPartProgressRequest;
import com.example.learningApp.dto.response.lesson.LessonPartProgressResponse;
import com.example.learningApp.service.course.section.lesson.UserLessonPartProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lesson-part-progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLessonPartProgressController {

    UserLessonPartProgressService progressService;

    /* ===================== UPDATE PROGRESS ===================== */

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @RequestBody UpdateLessonPartProgressRequest request
    ) {

        progressService.updateProgress(request);

        return ResponseEntity.ok(
                ApiResponse.success("Update lesson part progress successfully", null)
        );
    }

    /* ===================== GET PROGRESS (RESUME) ===================== */

    @GetMapping("/{lessonPartId}")
    public ResponseEntity<ApiResponse<LessonPartProgressResponse>> getProgress(
            @PathVariable String lessonPartId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson part progress successfully",
                        progressService.getProgress(lessonPartId)
                )
        );
    }
}
