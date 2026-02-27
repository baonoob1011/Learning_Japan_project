package com.example.learningApp.dto.request.course;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.LessonProcess;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateCourseRequest {

    private String title;

    private String description;
    private Boolean isPaid;
    private Long price;
    private JLPTLevel level;
    private LessonProcess lessonProcess;
    private MultipartFile image; // ✅ thêm dòng này

}
