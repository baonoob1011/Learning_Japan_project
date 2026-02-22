package com.example.learningApp.controller.progress;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.progress.DailyProgressResponse;
import com.example.learningApp.service.progress.UserLearningProgressService;
import com.example.learningApp.dto.response.dashboard.UserLearningDashboardResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-progress")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER_VIP')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLearningController {
     UserLearningProgressService service;

    @GetMapping
    public ResponseEntity<ApiResponse<UserLearningDashboardResponse>> getDashboard() {
        UserLearningDashboardResponse res =
                service.getDashboard();

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard retrieved", res)
        );
    }


    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyProgressResponse>>> getDailyProgress(
            @RequestParam(defaultValue = "7") int days
    ) {

        List<DailyProgressResponse> data =
                service.getDailyProgress(days);

        return ResponseEntity.ok(
                ApiResponse.success("Daily progress retrieved", data)
        );
    }
}
