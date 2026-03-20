package com.example.learningApp.dto.request.review;

import com.example.learningApp.enums.ReviewGrade;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeReviewRequest {
    @NotNull
    private ReviewGrade grade;
}

