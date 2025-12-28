package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "decks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name; // Tên bộ thẻ (VD: Từ vựng Video N3)

    @Column(name = "is_system")
    private boolean isSystem = false; // True nếu là bộ thẻ mặc định của App

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
