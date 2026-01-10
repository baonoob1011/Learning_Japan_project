package com.example.learningApp.dto.response.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuggestResponse {
    private String id;
    private String title;
}
