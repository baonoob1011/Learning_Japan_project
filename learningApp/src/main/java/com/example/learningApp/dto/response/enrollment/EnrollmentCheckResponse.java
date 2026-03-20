package com.example.learningApp.dto.response.enrollment;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCheckResponse {

    private String courseId;
    private Boolean enrolled;
}
