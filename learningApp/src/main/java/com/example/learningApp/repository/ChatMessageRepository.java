package com.example.learningApp.repository;

import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.entity.ChatRoom;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
        Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);

        Optional<ChatMessage> findTopByRoomIdOrderBySentAtDesc(String roomId);

        @Query("""
                        SELECT COUNT(m)
                        FROM ChatMessage m
                        WHERE m.room.id = :roomId
                        AND m.sender.id <> :userId
                        AND m.isRead = false
                        """)
        int countUnreadMessages(@Param("roomId") String roomId,
                        @Param("userId") String userId);

        long countByRoomIdAndSenderIdNotAndIsReadFalse(String roomId, String userId);

}

