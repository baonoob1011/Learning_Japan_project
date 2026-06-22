package com.example.learningApp.controller.progress;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.progress.SkillProgressResponse;
import com.example.learningApp.service.progress.ProgressTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/skill-progress")
@RequiredArgsConstructor
public class SkillProgressController {

    private final ProgressTrackingService progressTrackingService;

    // ================================
    // 🔥 GET USER SKILL DASHBOARD
    // ================================
    @GetMapping
    public ResponseEntity<ApiResponse<SkillProgressResponse>> getSkillProgress() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Skill progress fetched successfully",
                        progressTrackingService.getSkillProgress()
                )
        );
    }
}
