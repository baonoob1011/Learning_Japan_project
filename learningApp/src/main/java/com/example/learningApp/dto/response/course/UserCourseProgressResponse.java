package com.example.learningApp.dto.response.course;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserCourseProgressResponse {

    private String courseId;
    private double percent;
    private boolean completed;
    private LocalDateTime completedAt;
}
