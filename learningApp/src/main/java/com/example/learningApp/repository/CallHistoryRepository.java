package com.example.learningApp.repository;

import com.example.learningApp.entity.CallHistory;
import com.example.learningApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallHistoryRepository extends JpaRepository<CallHistory, String> {
    
    @Query("SELECT c FROM CallHistory c WHERE c.caller = :user OR c.receiver = :user ORDER BY c.timestamp DESC")
    List<CallHistory> findByCallerOrReceiver(@Param("user") User user);

    @Query("SELECT c FROM CallHistory c WHERE (c.caller = :u1 AND c.receiver = :u2) OR (c.caller = :u2 AND c.receiver = :u1) ORDER BY c.timestamp DESC")
    List<CallHistory> findBetweenUsers(@Param("u1") User u1, @Param("u2") User u2);
}

