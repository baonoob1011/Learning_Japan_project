package com.example.learningApp.dto.response.video;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoProgressResponse {

    String videoId;
    Long lastPositionSeconds;
    Long totalWatchedSeconds;
    boolean completed;
    Instant lastWatchedAt;
}

