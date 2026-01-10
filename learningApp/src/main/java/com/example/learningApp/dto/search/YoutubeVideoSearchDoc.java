package com.example.learningApp.dto.search;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeVideoSearchDoc {
    private String id;
    private String title;
    private String level;
    private String videoTag;
}
