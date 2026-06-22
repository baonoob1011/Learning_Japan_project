package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private String type = "TEXT"; // TEXT, MISSED_CALL, COMPLETED_CALL

    private String callType; // AUDIO, VIDEO
    private String callStatus; // MISSED, COMPLETED, REJECTED
    private String callSessionId;

    private LocalDateTime sentAt;

    @Builder.Default
    private Boolean isRead = false;
}
