package com.example.learningApp.dto.response.section;

import com.example.learningApp.enums.LessonLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionResponse {
    private String id;
    private String title;
    private LessonLevel lessonLevel;
}
