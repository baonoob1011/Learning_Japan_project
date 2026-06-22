package com.example.learningApp.service.payment;

import com.example.learningApp.entity.Order;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.repository.OrderRepository;
import com.example.learningApp.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRetryExpiryJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentRetryRedisService retryRedisService;

    @Scheduled(cron = "0 * * * * *")
    public void expireOldRetryOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<Order> candidates = orderRepository.findByStatusInAndCreatedAtBefore(
                List.of(PaymentStatus.PENDING, PaymentStatus.FAILED),
                threshold
        );

        for (Order order : candidates) {
            if (retryRedisService.isRetryExpired(order.getId())) {
                orderService.markOrderExpired(order.getId());
                retryRedisService.markRetryExpired(order.getId());
            }
        }
    }
}

