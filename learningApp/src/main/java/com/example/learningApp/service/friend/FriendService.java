package com.example.learningApp.service.friend;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.friend.FriendRequestDTO;
import com.example.learningApp.entity.FriendRequest;
import com.example.learningApp.entity.Friendship;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.FriendRequestStatus;
import com.example.learningApp.repository.FriendRequestRepository;
import com.example.learningApp.repository.FriendshipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final EntityFinder finder;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Map<String, String> sendRequest(String receiverId) {
        User sender = finder.userById();
        User receiver = finder.userId(receiverId);

        if (sender.getId().equals(receiverId)) {
            throw new RuntimeException( "Cannot send friend request to yourself");
        }

        if (friendshipRepository.areFriends(sender, receiver)) {
            throw new RuntimeException("You are already friends");
        }

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)) {
            throw new RuntimeException( "Friend request already sent");
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        request = friendRequestRepository.save(request);

        // Notify receiver via WebSocket
        FriendRequestDTO dto = FriendRequestDTO.builder()
                .requestId(request.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderAvatar(sender.getAvatarUrl())
                .build();

        messagingTemplate.convertAndSend("/topic/friend-request/" + receiverId, dto);

        return Map.of("requestId", request.getId());
    }

    @Transactional
    public void acceptRequest(String requestId) {
        User currentUser = finder.userById();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException( "Friend request not found"));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot accept this request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException( "Request is not pending");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Create friendship
        Friendship friendship = Friendship.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void rejectRequest(String requestId) {
        User currentUser = finder.userById();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException( "Friend request not found"));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot reject this request");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException( "Request is not pending");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);
    }

    public Map<String, String> getStatus(String userId) {
        User currentUser = finder.userById();
        User otherUser = finder.userId(userId);

        if (friendshipRepository.areFriends(currentUser, otherUser)) {
            return Map.of("status", "FRIENDS");
        }

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(currentUser, otherUser,
                FriendRequestStatus.PENDING)) {
            return Map.of("status", "PENDING_SENT");
        }

        if (friendRequestRepository.existsBySenderAndReceiverAndStatus(otherUser, currentUser,
                FriendRequestStatus.PENDING)) {
            return Map.of("status", "PENDING_RECEIVED");
        }

        return Map.of("status", "NONE");
    }
}
