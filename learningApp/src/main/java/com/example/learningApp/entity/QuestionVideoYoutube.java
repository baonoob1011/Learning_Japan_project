package com.example.learningApp.entity;
import com.example.learningApp.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionVideoYoutube {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id")
    private YoutubeTranscript transcript; // câu hỏi gắn với đoạn subtitle

    @Column(columnDefinition = "TEXT")
    private String questionText;

    private String questionType; // MCQ, FILL_BLANK, LISTENING

    private Integer correctOptionIndex;

    private Instant createdAt;
}

