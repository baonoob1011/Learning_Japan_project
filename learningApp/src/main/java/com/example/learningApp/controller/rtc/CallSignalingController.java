package com.example.learningApp.controller.rtc;

import com.example.learningApp.dto.CallSignal;
import com.example.learningApp.dto.IncomingCallNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CallSignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ─────────────────────────────────────────────────────────────────────────
    // ✅ FIX: @DestinationVariable KHÔNG hoạt động với /app/call.offer
    // Phải lấy roomId từ payload (signal.getRoomId()) rồi route thủ công
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Bên GỌI gửi lên: /app/call.offer
     * Controller broadcast đến: /topic/call/{roomId}
     * Cả 2 bên subscribe: /topic/call/{roomId}
     */
    @MessageMapping("/call.offer")
    public void handleOffer(@Payload CallSignal signal) {
        // Lấy roomId từ body payload, broadcast đến đúng room
        messagingTemplate.convertAndSend(
                "/topic/call/" + signal.getRoomId(),
                signal
        );
    }

    @MessageMapping("/call.answer")
    public void handleAnswer(@Payload CallSignal signal) {
        messagingTemplate.convertAndSend(
                "/topic/call/" + signal.getRoomId(),
                signal
        );
    }

    @MessageMapping("/call.ice")
    public void handleIce(@Payload CallSignal signal) {
        messagingTemplate.convertAndSend(
                "/topic/call/" + signal.getRoomId(),
                signal
        );
    }

    @MessageMapping("/call.end")
    public void handleEnd(@Payload CallSignal signal) {
        messagingTemplate.convertAndSend(
                "/topic/call/" + signal.getRoomId(),
                signal
        );
    }

    /**
     * Bên GỌI gửi notify cuộc gọi đến cho bên NHẬN:
     * Frontend gửi lên: /app/call.incoming
     * Backend gửi tới: /topic/call/incoming/{receiverId}
     * Bên NHẬN subscribe: /topic/call/incoming/{myUserId}
     */
    @MessageMapping("/call.incoming")
    public void notifyIncoming(@Payload IncomingCallNotification notification) {
        messagingTemplate.convertAndSend(
                "/topic/call/incoming/" + notification.getReceiverId(),
                notification
        );
    }
}