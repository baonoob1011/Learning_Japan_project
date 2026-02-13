package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_section_progress",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "section_id"}
        )
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSectionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Double progressPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    private Boolean completed;

    private LocalDateTime completedAt;
}
