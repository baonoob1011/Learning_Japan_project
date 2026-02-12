package com.example.learningApp.repository;

import com.example.learningApp.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomMemberRepository
        extends JpaRepository<ChatRoomMember, String> {
    List<ChatRoomMember> findByUserId(String userId);

    List<ChatRoomMember> findByRoomId(String roomId);

    boolean existsByRoomIdAndUserId(String roomId, String userId);
}
