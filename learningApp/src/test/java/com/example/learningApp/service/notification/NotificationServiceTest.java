package com.example.learningApp.service.notification;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.entity.Notification;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @Mock
    private EntityFinder finder;

    @Mock
    private NotificationSocketService socketService;

    @InjectMocks
    private NotificationService service;

    @Test
    void getUnreadCount_shouldReturnRepositoryValue() {
        User user = User.builder().id("u1").build();
        when(finder.userById()).thenReturn(user);
        when(repo.countByUserAndReadFalse(user)).thenReturn(7L);

        assertEquals(7L, service.getUnreadCount());
    }

    @Test
    void markAsRead_shouldUpdateReadFlag() {
        User user = User.builder().id("u1").build();
        Notification n = new Notification();
        n.setId("n1");
        n.setRead(false);

        when(finder.userById()).thenReturn(user);
        when(repo.findByIdAndUser("n1", user)).thenReturn(Optional.of(n));

        service.markAsRead("n1");

        assertEquals(true, n.isRead());
        verify(repo).save(any(Notification.class));
    }
}
