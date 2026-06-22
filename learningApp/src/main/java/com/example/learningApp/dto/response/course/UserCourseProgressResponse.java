package com.example.learningApp.dto.response.course;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserCourseProgressResponse {

    private String id;
    private double percent;
    private boolean completed;
    private LocalDateTime completedAt;

    private CourseResponse course;
}

