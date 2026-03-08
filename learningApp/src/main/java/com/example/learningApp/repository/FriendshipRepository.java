package com.example.learningApp.repository;

import com.example.learningApp.entity.Friendship;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    Optional<Friendship> findByUser1AndUser2(User user1, User user2);

    List<Friendship> findByUser2AndStatus(
            User user,
            FriendRequestStatus status);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.user1 = :user1 AND f.user2 = :user2) OR (f.user1 = :user2 AND f.user2 = :user1)")
    boolean areFriends(User user1, User user2);
}
