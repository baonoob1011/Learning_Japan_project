package com.example.learningApp.service.call;

import com.example.learningApp.entity.CallHistory;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.CallHistoryRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.ChatRoomRepository;
import com.example.learningApp.repository.ChatMessageRepository;
import com.example.learningApp.entity.ChatRoom;
import com.example.learningApp.entity.ChatMessage;
import com.example.learningApp.dto.response.chat.ChatMessageResponse;
import com.example.learningApp.mapper.ChatMessageMapper;
import com.example.learningApp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallHistoryService {

    private final CallHistoryRepository callHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public CallHistory saveCallRecord(String callerId, String receiverId, String type, String status, Integer duration,
            String roomId) {
        User caller = userRepository.findById(callerId).orElse(null);
        User receiver = userRepository.findById(receiverId).orElse(null);

        if (caller == null || receiver == null)
            return null;

        CallHistory history = CallHistory.builder()
                .caller(caller)
                .receiver(receiver)
                .type(type)
                .status(status)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .roomId(roomId)
                .build();

        CallHistory saved = callHistoryRepository.save(history);

        // 1. Tạo thông báo cho cuộc gọi nhỡ/từ chối
        if ("MISSED".equals(status)) {
            notificationService.create(receiver, "📞 Cuộc gọi nhỡ", "Bạn có cuộc gọi nhỡ từ " + caller.getFullName());
        } else if ("REJECTED".equals(status)) {
            notificationService.create(caller, "📞 Cuộc gọi bị từ chối",
                    receiver.getFullName() + " đã từ chối cuộc gọi");
        }

        // 2. Lưu vào tin nhắn chat để hiển thị trong lịch sử chat
        saveAsChatMessage(caller, receiver, type, status, duration, saved.getId());

        return saved;
    }

    private void saveAsChatMessage(User caller, User receiver, String type, String status, Integer duration,
            String sessionId) {
        String privateKey = caller.getId().compareTo(receiver.getId()) < 0
                ? caller.getId() + "_" + receiver.getId()
                : receiver.getId() + "_" + caller.getId();

        ChatRoom room = chatRoomRepository.findByPrivateKey(privateKey).orElse(null);
        if (room == null)
            return; // Nếu chưa bao giờ chat thì thôi (hoặc có thể tạo mới)

        String content;
        String msgType = ("MISSED".equals(status) || "REJECTED".equals(status) || "CANCELLED".equals(status))
                ? "MISSED_CALL"
                : "CALL_LOG";

        // Content dự phòng cho client cũ hoặc để lưu log đơn giản
        if ("COMPLETED".equals(status)) {
            content = "📞 Cuộc gọi " + ("VIDEO".equals(type) ? "video" : "thoại") + " (" + (duration / 60) + ":"
                    + String.format("%02d", (duration % 60)) + ")";
            msgType = "TEXT";
        } else if ("MISSED".equals(status) || "REJECTED".equals(status) || "CANCELLED".equals(status)) {
            content = "📞 Cuộc gọi nhỡ";
        } else {
            content = "📞 Cuộc gọi đã kết thúc";
        }

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(caller)
                .content(content)
                .type(msgType)
                .callType(type)
                .callStatus(status)
                .callSessionId(sessionId)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        chatMessageRepository.save(message);

        // Gửi qua websocket để cập nhật ngay lập tức
        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(message);
        response.setReceiverId(receiver.getId()); // Set receiverId for notification logic
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), response);
    }

    public List<CallHistory> getCallHistoryForUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return List.of();
        return callHistoryRepository.findByCallerOrReceiver(user);
    }
}
