package com.example.learningApp.controller.course.enrollment;


import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.dto.response.enrollment.EnrollmentCheckResponse;
import com.example.learningApp.service.course.errollment.EnrollmentCheckService;
import lombok.AccessLevel;
import com.example.learningApp.service.course.enrollment.EnrollmentQueryService ;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnrollmentController {

    EnrollmentCheckService enrollmentCheckService;
EnrollmentQueryService enrollmentQueryService;

    /* ===================== CHECK ENROLLMENT ===================== */

    @GetMapping("/check/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentCheckResponse>> checkEnrollment(
            @PathVariable String courseId
    ) {

        boolean enrolled =
                enrollmentCheckService.isUserEnrolled(courseId);

        EnrollmentCheckResponse response =
                EnrollmentCheckResponse.builder()
                        .courseId(courseId)
                        .enrolled(enrolled)
                        .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Check enrollment successfully",
                        response
                )
        );
    }
    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getMyCourses() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get my courses successfully",
                        enrollmentQueryService.getMyCourses()
                )
        );
    }
}