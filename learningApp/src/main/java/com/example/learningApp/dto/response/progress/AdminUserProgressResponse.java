package com.example.learningApp.dto.response.progress;

import com.example.learningApp.dto.response.dashboard.UserLearningDashboardResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserProgressResponse {

    private Integer totalExamsTaken;
    private Integer totalQuestionsDone;
    private Integer correctQuestions;
    private Double accuracy;      // Độ chính xác trung bình
    private String lastLevel;     // Level mới học gần nhất
    private List<LevelDetailResponse> levels;
    private UserResponse user;

}
