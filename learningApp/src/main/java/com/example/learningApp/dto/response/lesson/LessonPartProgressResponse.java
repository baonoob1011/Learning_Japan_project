package com.example.learningApp.dto.response.lesson;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LessonPartProgressResponse {

    private Double progressPercent;
    private Double lastWatchedSecond;
    private Boolean completed;
}

