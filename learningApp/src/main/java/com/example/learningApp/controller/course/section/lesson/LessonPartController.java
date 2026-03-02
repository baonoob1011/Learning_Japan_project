package com.example.learningApp.controller.course.section.lesson;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.course.section.lesson.CreateLessonPartRequest;
import com.example.learningApp.dto.request.course.section.lesson.UpdateLessonPartRequest;
import com.example.learningApp.dto.response.lesson.LessonPartResponse;
import com.example.learningApp.service.course.section.lesson.LessonPartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-part")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LessonPartController {

    LessonPartService lessonPartService;

    /* ===================== CREATE ===================== */

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createLessonPart(
            @RequestBody CreateLessonPartRequest request
    ) throws IOException, InterruptedException {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create lesson part successfully",
                        lessonPartService.createLessonPart(request)
                )
        );
    }
    /* ===================== UPDATE ===================== */

    @PutMapping("/{lessonPartId}")
    public ResponseEntity<ApiResponse<String>> updateLessonPart(
            @PathVariable String lessonPartId,
            @RequestBody UpdateLessonPartRequest request
    ) throws IOException, InterruptedException {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update lesson part successfully",
                        lessonPartService.updateLessonPart(lessonPartId, request)
                )
        );
    }
    /* ===================== GET BY LESSON ===================== */

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ApiResponse<List<LessonPartResponse>>> getByLesson(
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson parts successfully",
                        lessonPartService.getByLesson(lessonId)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{lessonPartId}")
    public ResponseEntity<ApiResponse<LessonPartResponse>> getDetail(
            @PathVariable String lessonPartId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson part successfully",
                        lessonPartService.getDetail(lessonPartId)
                )
        );
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{lessonPartId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String lessonPartId
    ) {

        lessonPartService.deleteLessonPart(lessonPartId);

        return ResponseEntity.ok(
                ApiResponse.success("Delete lesson part successfully", null)
        );
    }
}
