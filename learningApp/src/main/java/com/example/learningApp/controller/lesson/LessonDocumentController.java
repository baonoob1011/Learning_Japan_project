package com.example.learningApp.controller.lesson;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.lesson.LessonDocumentResponse;
import com.example.learningApp.service.lesson.LessonDocumentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-document")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LessonDocumentController {

    LessonDocumentService lessonDocumentService;

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> create(
            @RequestParam String lessonId,
            @RequestParam String title,
            @RequestParam Integer documentOrder,
            @RequestParam MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create lesson document successfully",
                        lessonDocumentService.create(
                                lessonId,
                                title,
                                documentOrder,
                                file
                        )
                )
        );
    }

    /* ===================== GET BY LESSON ===================== */

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<ApiResponse<List<LessonDocumentResponse>>> getByLesson(
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson documents successfully",
                        lessonDocumentService.getByLesson(lessonId)
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<LessonDocumentResponse>> getDetail(
            @PathVariable String documentId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get lesson document successfully",
                        lessonDocumentService.getDetail(documentId)
                )
        );
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String documentId
    ) {

        lessonDocumentService.delete(documentId);

        return ResponseEntity.ok(
                ApiResponse.success("Delete lesson document successfully", null)
        );
    }
}
