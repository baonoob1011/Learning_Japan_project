package com.example.learningApp.dto.request.video;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoProgressRequest {

    @NotNull
    private String videoId;
    private Long lastPositionSeconds;   // giây hiện tại
    private Long watchedSecondsDelta;   // xem thêm bao nhiêu giây

}

