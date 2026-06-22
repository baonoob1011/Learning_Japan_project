package com.example.learningApp.dto.response.course;

import com.example.learningApp.entity.Section;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.LessonProcess;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseResponse {
    private String id;
    private String title;
    private String description;
    private JLPTLevel level;
    private LessonProcess lessonProcess;
    private String createdBy;
    private Boolean isPaid;
    private Long price;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

