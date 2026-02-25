package com.example.learningApp.repository;

import com.example.learningApp.entity.FriendRequest;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);
}
