package com.example.learningApp.controller.notification;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.notification.NotificationResponse;
import com.example.learningApp.service.notification.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private static final String DEPLOY_MARKER = "UNREAD_COUNT_V2_2026-03-22";

    NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<NotificationResponse> res = notificationService.getNotifications(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(ApiResponse.success("Get notifications success", res));
    }

    @RequestMapping(value = {"/unread-count", "/unread-count/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        log.warn("[{}] hit /api/v1/notifications/unread-count", DEPLOY_MARKER);
        System.out.println(">>> " + DEPLOY_MARKER + " | NotificationController#getUnreadCount called");
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", Map.of("unreadCount", notificationService.getUnreadCount())));
    }

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @RequestMapping(value = "/read-all", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications() {
        notificationService.deleteAll();
        return ResponseEntity.ok(ApiResponse.success("All notifications deleted", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }
}

