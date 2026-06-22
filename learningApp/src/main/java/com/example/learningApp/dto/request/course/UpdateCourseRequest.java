package com.example.learningApp.dto.request.course;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.LessonProcess;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateCourseRequest {

    private String title;

    private String description;

    private JLPTLevel level;

    private LessonProcess lessonProcess;

    private Long price;


}
