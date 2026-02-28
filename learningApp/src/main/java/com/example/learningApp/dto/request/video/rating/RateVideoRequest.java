package com.example.learningApp.dto.request.video.rating;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RateVideoRequest {

    @NotBlank
    private String videoId;

    @Min(1)
    @Max(5)
    private int rating;
}