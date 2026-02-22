package com.example.learningApp.controller.course;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.course.UserCourseProgressResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.CourseRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.course.UserCourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class UserCourseProgressController {

    private final UserCourseProgressService userCourseProgressService;


    @GetMapping("/{courseId}/progress")
    public ResponseEntity<ApiResponse<UserCourseProgressResponse>> getCourseProgress(
            @PathVariable String courseId
    ) {

        UserCourseProgressResponse response =
                userCourseProgressService.getCourseProgress(courseId);

        return ResponseEntity.ok(
                ApiResponse.success("Get course progress successfully", response)
        );
    }

    @GetMapping("/my-progress")
    public ResponseEntity<ApiResponse<List<UserCourseProgressResponse>>> getMyCourseProgress() {
        List<UserCourseProgressResponse> responses =
                userCourseProgressService.getAllCourseProgressByUserId();

        return ResponseEntity.ok(
                ApiResponse.success("Get my course progress successfully", responses)
        );
    }
}
