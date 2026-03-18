package com.example.learningApp.entity;

import com.example.learningApp.enums.JLPTLevel;
import com.example.learningApp.enums.VideoStatus;
import com.example.learningApp.enums.VideoTag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "youtube_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "videoTrackings", "users", "vocabs",
                "youtubeTranscripts" })
public class YoutubeVideo {
        @Id
        private String id; // YouTube video ID
        private String title;
        @Column(columnDefinition = "TEXT")
        private String description;
        private String thumbnailUrl;

        @Enumerated(EnumType.STRING)
        private VideoTag videoTag;

        @Enumerated(EnumType.STRING)
        private JLPTLevel level;

        @Enumerated(EnumType.STRING)
        @Builder.Default
        private VideoStatus videoStatus = VideoStatus.NEW;

        @OneToMany(mappedBy = "video", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JsonIgnore
        @Builder.Default
        Set<UserVideoTracking> videoTrackings = new HashSet<>();

        private String channelTitle;
        private String duration; // PT2H35M54S
        private Instant publishedAt;
        private String s3Url; // URL sau khi upload lên S3
        private String urlVideo;

        @ManyToMany(mappedBy = "savedVideos", fetch = FetchType.LAZY)
        @JsonIgnore
        @Builder.Default
        private Set<User> users = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "video_vocab", joinColumns = @JoinColumn(name = "video_id"), inverseJoinColumns = @JoinColumn(name = "vocab_id"))
        @Builder.Default
        private Set<Vocab> vocabs = new HashSet<>();
        @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
        @Builder.Default
        private List<YoutubeTranscript> youtubeTranscripts = new ArrayList<>();// URL sau khi upload lên S3
        private Instant createdAt;
        private Instant updatedAt;
}
