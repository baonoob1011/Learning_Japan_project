package com.example.learningApp.dto.event;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YoutubeTranscribeMessage {
    private String youtubeUrl;
    private String videoId;
    private String languageCode;
}
