package com.example.learningApp.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "user_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Question question;

    private String selectedAnswer;

    private boolean correct;

    private LocalDateTime answeredAt;
}
