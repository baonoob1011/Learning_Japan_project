package com.example.learningApp.entity;
import com.example.learningApp.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_question_id", nullable = false)
    private QuestionVideoYoutube question;

    @Column(columnDefinition = "TEXT")
    private String content;
    // ✅ Thêm dòng này
    private boolean correct;
    private Integer optionIndex; // 0=A,1=B,2=C,3=D
}

