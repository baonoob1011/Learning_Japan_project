package com.example.learningApp.dto.response.video.rating;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoRatingResponse {

    private double averageRating;
    private int totalRatings;
}
