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

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class UserCourseProgressController {

    private final UserCourseProgressService userCourseProgressService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @GetMapping("/{courseId}/progress")
    public ResponseEntity<ApiResponse<UserCourseProgressResponse>> getCourseProgress(
            @PathVariable String courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        String userId = jwt.getSubject();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserCourseProgressResponse response =
                userCourseProgressService.getCourseProgress(user, course);

        return ResponseEntity.ok(
                ApiResponse.success("Get course progress successfully", response)
        );
    }
}
