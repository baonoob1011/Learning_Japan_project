package com.example.learningApp.dto.request.lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLessonPartProgressRequest {

    private String lessonPartId;
    private Double progressPercent;
    private Double lastWatchedSecond;
}
