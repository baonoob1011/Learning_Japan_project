package com.example.learningApp.repository;

import com.example.learningApp.entity.ChatRoomMember;
import com.example.learningApp.entity.User;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository
        extends JpaRepository<ChatRoomMember, String> {
    Optional<ChatRoomMember> findByRoomIdAndUserId(String roomId, String userId);

    int countByRoomId(String roomId);

    List<ChatRoomMember> findByUserId(String userId);

    List<ChatRoomMember> findByRoomId(String roomId);

    boolean existsByRoomIdAndUserId(String roomId, String userId);

    @Query("""
                SELECT DISTINCT m2.user
                FROM ChatRoomMember m1
                JOIN ChatRoomMember m2 ON m1.room.id = m2.room.id
                WHERE m1.user.id = :currentUserId
                  AND m1.room.roomType = com.example.learningApp.enums.RoomType.PRIVATE
                  AND m2.user.id <> :currentUserId
            """)
    List<User> findPrivateChatPartners(@Param("currentUserId") String currentUserId);

    @Query("""
                SELECT u,
                       (
                           SELECT m.content
                           FROM ChatMessage m
                           WHERE m.room.id = r.id
                           AND m.sentAt = (
                                SELECT MAX(m2.sentAt)
                                FROM ChatMessage m2
                                WHERE m2.room.id = r.id
                           )
                       ),
                       (
                           SELECT MAX(m3.sentAt)
                           FROM ChatMessage m3
                           WHERE m3.room.id = r.id
                       ),
                       r.id
                FROM ChatRoom r
                JOIN r.members rm
                JOIN rm.user u
                WHERE r.roomType = com.example.learningApp.enums.RoomType.PRIVATE
                  AND r.id IN (
                        SELECT rm2.room.id
                        FROM ChatRoomMember rm2
                        WHERE rm2.user.id = :currentUserId
                  )
                  AND u.id <> :currentUserId
            """)
    List<Object[]> findPrivateChatPreview(String currentUserId);

    @Modifying
    @Transactional
    void deleteByRoomId(String roomId);
}
