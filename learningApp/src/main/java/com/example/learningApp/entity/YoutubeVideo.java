package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "youtube_videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoutubeVideo {
    @Id
    private String id;                 // YouTube video ID
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String thumbnailUrl;
    private String channelTitle;
    private String duration;           // PT2H35M54S
    private Instant publishedAt;
    private String s3Url;              // URL sau khi upload lên S3
    private String urlVideo;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<YoutubeTranscript> youtubeTranscripts = new ArrayList<>();// URL sau khi upload lên S3
    private Instant createdAt;
    private Instant updatedAt;
}
