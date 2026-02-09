package com.example.learningApp.entity;

import com.example.learningApp.enums.LearningStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vocab")
public class Vocab {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String surface;       // từ gốc
    private String romaji;        // phiên âm
    private String translated;
    private String reading;
    private String partOfSpeech;  // loại từ
    private String targetDefs;    // nghĩa ngôn ngữ đích
    private String explain;
    private String audioUrl;      // đường dẫn audio trên S3
    @ManyToMany(mappedBy = "vocabs")
    private Set<YoutubeVideo> videos = new HashSet<>();

    @ManyToMany(mappedBy = "savedVocabs")
    private Set<User> users = new HashSet<>();

    // ===== Vocab learning progress =====
    @OneToMany(
            mappedBy = "vocab",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<UserVocabProgress> vocabProgresses;
}