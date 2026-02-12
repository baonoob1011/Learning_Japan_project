package com.example.learningApp.entity;

import com.example.learningApp.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name; // PRIVATE có thể null

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private LocalDateTime createdAt;

    @Column(unique = true)
    private String privateKey;


    // 1 room có nhiều member
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> members;

    // 1 room có nhiều message
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;
}
