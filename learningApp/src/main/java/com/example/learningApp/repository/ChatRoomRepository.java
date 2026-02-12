package com.example.learningApp.repository;

import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.Course;
import com.example.learningApp.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("""
    SELECT r FROM ChatRoom r
    WHERE r.roomType = com.example.learningApp.enums.RoomType.PRIVATE
    AND r.id IN (
        SELECT m.room.id
        FROM ChatRoomMember m
        WHERE m.user.id IN (:user1, :user2)
        GROUP BY m.room.id
        HAVING COUNT(DISTINCT m.user.id) = 2
    )
""")
    Optional<ChatRoom> findPrivateRoomBetweenUsers(
            String user1,
            String user2
    );
    Optional<ChatRoom> findByPrivateKey(String privateKey);


}
