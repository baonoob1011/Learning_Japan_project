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
public class UserAnswerVideoYoutube {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_question_id")
    private QuestionVideoYoutube question;

    private Integer selectedOptionIndex;

    private Boolean isCorrect;

    private Instant answeredAt;
}

