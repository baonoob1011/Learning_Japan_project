package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabularies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String word;

    private String hiragana;

    @Column(name = "han_viet")
    private String hanViet;

    @Column(columnDefinition = "TEXT")
    private String meaning; // Nghĩa tiếng Việt

    @Column(name = "audio_url")
    private String audioUrl;
}
