package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Người comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Video được comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideo video;

    // Reply comment (nếu là reply)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private VideoComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<VideoComment> replies = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}