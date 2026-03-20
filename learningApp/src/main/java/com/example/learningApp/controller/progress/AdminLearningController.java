package com.example.learningApp.controller.progress;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.progress.AdminUserProgressResponse;
import com.example.learningApp.dto.response.progress.DailyProgressResponse;
import com.example.learningApp.service.progress.AdminLearningService;
import com.example.learningApp.service.progress.UserLearningProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/learning-progress")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminLearningController {

    AdminLearningService adminLearningService;

    UserLearningProgressService service;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserProgressResponse>> getUserProgress(@PathVariable String userId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User progress retrieved successfully",
                        adminLearningService.getUserProgress(userId)
                )
        );
    }


    @GetMapping("/{userId}/daily")
    public ResponseEntity<ApiResponse<List<DailyProgressResponse>>> getUserDailyProgress(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User daily progress retrieved successfully",
                        adminLearningService.getDailyProgress(userId, days)
                )
        );
    }

}

