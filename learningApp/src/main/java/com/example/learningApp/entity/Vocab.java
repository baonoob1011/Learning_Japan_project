package com.example.learningApp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vocab")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "vocabProgresses", "videos", "users" })
public class Vocab {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String surface; // từ gốc
    private String romaji; // phiên âm
    @Column(columnDefinition = "TEXT")
    private String translated;
    private String reading;
    private String partOfSpeech; // loại từ
    @Column(columnDefinition = "TEXT")
    private String targetDefs; // nghĩa ngôn ngữ đích
    @Column(columnDefinition = "TEXT")
    private String explain;
    @Column(columnDefinition = "TEXT")
    private String example;

    private String audioUrl; // đường dẫn audio trên S3
    @ManyToMany(mappedBy = "vocabs")
    @JsonIgnore
    @Builder.Default
    private Set<YoutubeVideo> videos = new HashSet<>();

    @ManyToMany(mappedBy = "savedVocabs")
    @JsonIgnore
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // ===== Vocab learning progress =====
    @OneToMany(mappedBy = "vocab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<UserVocabProgress> vocabProgresses = new HashSet<>();
}
