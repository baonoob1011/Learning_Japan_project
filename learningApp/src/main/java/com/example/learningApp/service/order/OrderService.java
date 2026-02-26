package com.example.learningApp.service.order;


import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VipPackage;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.repository.OrderRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VipPackageRepository;
import com.example.learningApp.service.vipPackage.VipPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VipPurchaseService vipPurchaseService;

    private final EntityFinder finder;

    /* ===================== CREATE ORDER ===================== */

    public List<OrderSuccessResponse> getMyOrders() {

        var currentUser = finder.userById();

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(order -> OrderSuccessResponse.builder()
                        .orderId(order.getId())
                        .orderCode(order.getOrderCode())
                        .amount(order.getAmount())
                        .paymentMethod(order.getPaymentMethod())
                        .transactionNo(order.getTransactionNo())
                        .paidAt(order.getPaidAt())
                        .expiredAt(order.getExpiredAt())
                        .vipPackageId(order.getVipPackage().getId())
                        .packageName(order.getVipPackage().getName())
                        .planType(order.getVipPackage().getPlanType().name())
                        .durationDays(order.getVipPackage().getDurationDays())
                        .build())
                .toList();
    }
    public OrderSuccessResponse getOrderDetail(String orderCode) {

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return OrderSuccessResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .amount(order.getAmount())
                .paymentMethod(order.getPaymentMethod())
                .transactionNo(order.getTransactionNo())
                .paidAt(order.getPaidAt())
                .expiredAt(order.getExpiredAt())
                .vipPackageId(order.getVipPackage().getId())
                .packageName(order.getVipPackage().getName())
                .planType(order.getVipPackage().getPlanType().name())
                .durationDays(order.getVipPackage().getDurationDays())
                .build();
    }

    public Order createPendingOrder(
            String vipPackageId,
            String orderCode,
            Long amount
    ) {

        var user=finder.userById();
        var vipPackage=finder.vipPackageById(vipPackageId);

        Order order = Order.builder()
                .user(user)
                .vipPackage(vipPackage)
                .orderCode(orderCode)
                .amount(amount)
                .paymentMethod("VNPAY")
                .status(PaymentStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }

    /* ===================== SUCCESS ===================== */

    public OrderSuccessResponse markOrderSuccess(
            String orderCode,
            String transactionNo
    ) {

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(PaymentStatus.SUCCESS);
        order.setTransactionNo(transactionNo);
        order.setPaidAt(LocalDateTime.now());

        Integer duration = order.getVipPackage().getDurationDays();

        if (duration != null) {
            order.setExpiredAt(LocalDateTime.now().plusDays(duration));
        }

        orderRepository.save(order);
        vipPurchaseService.purchaseVip(order.getVipPackage().getId(),order.getUser().getId());

        return OrderSuccessResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .amount(order.getAmount())
                .paymentMethod(order.getPaymentMethod())
                .transactionNo(order.getTransactionNo())
                .paidAt(order.getPaidAt())
                .expiredAt(order.getExpiredAt())
                .vipPackageId(order.getVipPackage().getId())
                .packageName(order.getVipPackage().getName())
                .planType(order.getVipPackage().getPlanType().name())
                .durationDays(order.getVipPackage().getDurationDays())
                .build();
    }


    /* ===================== FAILED ===================== */

    public void markOrderFailed(String orderCode) {

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(PaymentStatus.FAILED);

        orderRepository.save(order);
    }
}
