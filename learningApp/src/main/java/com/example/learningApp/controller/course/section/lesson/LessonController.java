package com.example.learningApp.controller.course.section.lesson;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.course.section.lesson.CreateLessonRequest;
import com.example.learningApp.dto.response.lesson.LessonResponse;
import com.example.learningApp.service.course.section.lesson.LessonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LessonController {

    LessonService lessonService;

    /* ===================== CREATE ===================== */

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createLesson(
            @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create lesson successfully",
                        lessonService.createLesson(request)
                )
        );
    }

    /* ===================== GET BY SECTION ===================== */

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getBySection(
            @PathVariable String sectionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lessons successfully",
                        lessonService.getLessonsBySection(sectionId)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> getDetail(
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson successfully",
                        lessonService.getLessonDetail(lessonId)
                )
        );
    }
    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<String>> updateLesson(
            @PathVariable String lessonId,
            @RequestBody CreateLessonRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update lesson successfully",
                        lessonService.updateLesson(lessonId, request)
                )
        );
    }
    /* ===================== DELETE ===================== */

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String lessonId
    ) {
        lessonService.deleteLesson(lessonId);

        return ResponseEntity.ok(
                ApiResponse.success("Delete lesson successfully", null)
        );
    }
}
