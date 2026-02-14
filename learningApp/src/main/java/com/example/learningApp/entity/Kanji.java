package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kanjis")
@Getter
@Setter
public class Kanji {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String character;   // 漢

    private String meaning;     // Chinese

    private String onyomi;
    private String kunyomi;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String strokeData;  // JSON chuẩn stroke
}
