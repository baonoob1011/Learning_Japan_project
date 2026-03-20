package com.example.learningApp.dto.response.lesson;

import com.example.learningApp.entity.Lesson;
import com.example.learningApp.enums.LessonPartType;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class LessonPartResponse {
    private String id;
    private LessonPartType lessonPartType;
    private String videoUrl;
    private String title;
    private String duration;           // PT2H35M54S
    private Integer partOrder;
}

