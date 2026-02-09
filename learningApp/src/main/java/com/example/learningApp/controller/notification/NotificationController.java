package com.example.learningApp.controller.notification;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.notìication.NotificationResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.service.notification.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    /**
     * 📥 GET + phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationResponse> res =
                notificationService.getNotifications(
                        user,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success("Get notifications success", res)
        );
    }

    /**
     * ✅ Mark 1 notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id
    ) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read", null)
        );
    }

    /**
     * ✅ Mark all as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read", null)
        );
    }

    /**
     * 🗑️ Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable String id
    ) {
        notificationService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.success("Notification deleted", null)
        );
    }
}
