package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String type; // VIDEO or VOICE

    @Column(nullable = false)
    private String status; // COMPLETED, MISSED, REJECTED, CANCELLED

    private Integer duration; // in seconds

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String roomId;
}

