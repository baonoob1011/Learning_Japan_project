package com.example.learningApp.dto.response.lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonResponse {

    private String id;

    private String title;
    private String videoUrl;
    private String duration;           // PT2H35M54S

    private Integer lessonOrder;

    private String description;
}
