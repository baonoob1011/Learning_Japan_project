package com.example.learningApp.service.chat.friend;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.friend.SendFriendRequest;
import com.example.learningApp.dto.request.friend.FriendActionRequest;
import com.example.learningApp.dto.response.friend.FriendRequestResponse;
import com.example.learningApp.dto.response.friend.FriendResponse;
import com.example.learningApp.entity.Friendship;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.FriendRequestStatus;
import com.example.learningApp.repository.ChatRoomRepository;
import com.example.learningApp.repository.FriendshipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final EntityFinder finder;
    private final SimpMessagingTemplate messagingTemplate;

    public List<FriendRequestResponse> getPendingRequests() {

        User currentUser = finder.userById();

        List<Friendship> pendingList = friendshipRepository.findByUser2AndStatus(
                currentUser,
                FriendRequestStatus.PENDING);

        return pendingList.stream().map(friendship -> {

            User sender = friendship.getUser1().equals(currentUser)
                    ? friendship.getUser2()
                    : friendship.getUser1();

            return FriendRequestResponse.builder()
                    .requestId(friendship.getId())
                    .senderId(sender.getId())
                    .senderName(sender.getFullName())
                    .senderAvatar(sender.getAvatarUrl())
                    .receiverId(currentUser.getId())
                    .receiverName(currentUser.getFullName())
                    .receiverAvatar(currentUser.getAvatarUrl())
                    .status(friendship.getStatus().name())
                    .createdAt(friendship.getCreatedAt())
                    .build();
        }).toList();
    }

    // =========================
    // SEND REQUEST
    // =========================
    @Transactional
    public FriendRequestResponse sendRequest(SendFriendRequest dto) {

        User sender = finder.userById();
        User receiver = finder.userId(dto.getReceiverId());

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        // đảm bảo không duplicate theo thứ tự id
        User u1 = sender;
        User u2 = receiver;

        if (u1.getId().compareTo(u2.getId()) > 0) {
            User temp = u1;
            u1 = u2;
            u2 = temp;
        }

        Friendship existing = friendshipRepository
                .findByUser1AndUser2(u1, u2)
                .orElse(null);

        if (existing != null) {
            if (existing.getStatus() == FriendRequestStatus.ACCEPTED) {
                throw new RuntimeException("Already friends");
            }
            if (existing.getStatus() == FriendRequestStatus.PENDING) {
                throw new RuntimeException("Friend request already pending");
            }
        }

        Friendship friendship = Friendship.builder()
                .user1(u1)
                .user2(u2)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendship = friendshipRepository.save(friendship);

        // build response
        FriendRequestResponse response = FriendRequestResponse.builder()
                .requestId(friendship.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName()) // đổi theo field của bạn
                .senderAvatar(sender.getAvatarUrl()) // đổi theo field của bạn
                .receiverId(receiver.getId())
                .receiverName(receiver.getFullName())
                .receiverAvatar(receiver.getAvatarUrl())
                .status(friendship.getStatus().name())
                .createdAt(friendship.getCreatedAt()) // nếu entity có field này
                .build();

        // gửi realtime cho người nhận
        messagingTemplate.convertAndSend(
                "/topic/friend-request/" + receiver.getId(),
                response);

        return response;
    }

    // =========================
    // ACCEPT
    // =========================
    @Transactional
    public void acceptRequest(FriendActionRequest dto) {

        User currentUser = finder.userById();

        Friendship friendship = friendshipRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (friendship.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException("Request not pending");
        }

        // chỉ người không gửi mới được accept
        if (friendship.getUser1().equals(currentUser)
                || friendship.getUser2().equals(currentUser)) {

            friendship.setStatus(FriendRequestStatus.ACCEPTED);
            friendshipRepository.save(friendship);
        } else {
            throw new RuntimeException("Not allowed");
        }

        messagingTemplate.convertAndSend(
                "/topic/friend-accepted/" + currentUser.getId(),
                Map.of("status", "ACCEPTED"));
    }

    // =========================
    // REJECT
    // =========================
    @Transactional
    public void rejectRequest(FriendActionRequest dto) {

        Friendship friendship = friendshipRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (friendship.getStatus() != FriendRequestStatus.PENDING) {
            throw new RuntimeException("Request not pending");
        }

        friendship.setStatus(FriendRequestStatus.REJECTED);
        friendshipRepository.save(friendship);
    }

    // =========================
    // CHECK STATUS
    // =========================
    public Map<String, String> getStatus(String userId) {

        User currentUser = finder.userById();
        User otherUser = finder.userId(userId);

        User u1 = currentUser;
        User u2 = otherUser;

        if (u1.getId().compareTo(u2.getId()) > 0) {
            User temp = u1;
            u1 = u2;
            u2 = temp;
        }

        Friendship friendship = friendshipRepository
                .findByUser1AndUser2(u1, u2)
                .orElse(null);

        if (friendship == null) {
            return Map.of("status", "NONE");
        }

        return Map.of("status", friendship.getStatus().name());
    }

    /**
     * 💔 Hủy kết bạn
     */
    @Transactional
    public void unfriend(String otherUserId) {
        User currentUser = finder.userById();
        User otherUser = finder.userId(otherUserId);

        // Đảm bảo u1.id < u2.id giống như khi lưu
        User u1 = currentUser.getId().compareTo(otherUser.getId()) < 0 ? currentUser : otherUser;
        User u2 = u1.equals(currentUser) ? otherUser : currentUser;

        friendshipRepository.findByUser1AndUser2(u1, u2).ifPresent(friendshipRepository::delete);

        // 🗑️ Đồng thời xóa luôn phòng chat private (nếu có)
        String privateKey = u1.getId() + "_" + u2.getId();
        chatRoomRepository.findByPrivateKey(privateKey).ifPresent(chatRoomRepository::delete);
        
        // Gửi tín hiệu để UI cập nhật (tùy chọn)
        messagingTemplate.convertAndSend("/topic/friend-unfriend/" + currentUser.getId(), 
            Map.of("targetUserId", otherUserId));
        messagingTemplate.convertAndSend("/topic/friend-unfriend/" + otherUserId, 
            Map.of("targetUserId", currentUser.getId()));
    }
}
