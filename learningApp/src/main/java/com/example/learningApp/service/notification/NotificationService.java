package com.example.learningApp.service.notification;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.notìication.NotificationResponse;
import com.example.learningApp.entity.Notification;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final EntityFinder finder;
    private final NotificationSocketService socketService;

    public void create(User user, String title, String content) {

        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setContent(content);
        n.setRead(false);
        repo.save(n);

        // 🔥 PUSH REALTIME
        socketService.push(user.getId(), n);
    }

    public Page<NotificationResponse> getNotifications(
            User user,
            Pageable pageable
    ) {
        return repo.findByUser(user, pageable)
                .map(NotificationResponse::new);
    }
    public void markAsRead(String notificationId) {
        var user=finder.userById();


        Notification n = repo.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        repo.save(n);
    }

    public void markAllAsRead() {
        var user=finder.userById();
        repo.markAllAsReadByUser(user);
    }

    public void delete(String notificationId) {
        var user=finder.userById();
        Notification n = repo.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        repo.delete(n);
    }
}
