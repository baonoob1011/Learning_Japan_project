package com.example.learningApp.service.notification;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.notification.NotificationResponse;
import com.example.learningApp.entity.Notification;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.NotificationType;
import com.example.learningApp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository repo;
    private final EntityFinder finder;
    private final NotificationSocketService socketService;

    public void create(User user, String title, String content) {
        create(user, title, content, NotificationType.SYSTEM, null);
    }

    public void create(User user, String title, String content, NotificationType type, String metadata) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type == null ? NotificationType.SYSTEM : type);
        n.setTitle(title);
        n.setContent(content);
        n.setMetadata(metadata);
        n.setRead(false);
        repo.save(n);

        socketService.push(user.getId(), n);
    }

    public PageResponse<NotificationResponse> getNotifications(Pageable pageable) {
        User user = finder.userById();
        Page<NotificationResponse> page = repo.findByUser(user, pageable)
                .map(NotificationResponse::new);

        return PageResponse.<NotificationResponse>builder()
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public void markAsRead(String notificationId) {
        User user = finder.userById();
        Notification n = repo.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        repo.save(n);
    }

    public void markAllAsRead() {
        User user = finder.userById();
        repo.markAllAsReadByUser(user);
    }

    public long getUnreadCount() {
        User user = finder.userById();
        long count = repo.countByUserAndIsReadFalse(user);
        System.out.println(
                "[NEW_CODE_VER_12:45] Fetching unread count for userId: " + user.getId()
                        + ", email: " + user.getEmail()
                        + ", Result: " + count);
        return count;
    }

    public void delete(String notificationId) {
        User user = finder.userById();
        Notification n = repo.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        repo.delete(n);
    }

    public void deleteAll() {
        User user = finder.userById();
        repo.deleteAllByUser(user);
    }
}
