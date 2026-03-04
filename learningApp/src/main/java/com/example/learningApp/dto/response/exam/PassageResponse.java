package com.example.learningApp.dto.response.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassageResponse {
    private String id;
    private String title;
    private String content;
    private Integer passageOrder;
}
