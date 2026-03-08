package com.example.learningApp.controller.course;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.course.CreateCourseRequest;
import com.example.learningApp.dto.request.course.UpdateCourseRequest;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.service.course.CourseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    CourseService courseService;

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> createCourse(
            @ModelAttribute CreateCourseRequest request    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create course successfully",
                        courseService.createCourse(request)
                )
        );
    }

    /* ===================== GET ALL ACTIVE ===================== */
    /* ===================== UPDATE ===================== */

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> updateCourse(
            @PathVariable String courseId,
            @RequestBody UpdateCourseRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update course successfully",
                        courseService.updateCourse(courseId, request)
                )
        );
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get courses successfully",
                        courseService.getActiveCourses()
                )
        );
    }

    /* ===================== GET DETAIL ===================== */

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDetail(
            @PathVariable String courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get course detail successfully",
                        courseService.getCourseDetail(courseId)
                )
        );
    }

    /* ===================== GET FULL TREE ===================== */
    /**
     * Course → Section → Lesson → SectionDocument
     */
    @GetMapping("/{courseId}/tree")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseTree(
            @PathVariable String courseId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get course tree successfully",
                        courseService.getCourseTree(courseId)
                )
        );
    }

    /* ===================== TOGGLE ACTIVE ===================== */

    @PutMapping("/{courseId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleCourse(
            @PathVariable String courseId
    ) {
        courseService.toggleActive(courseId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Toggle course successfully",
                        null
                )
        );
    }



}
