package com.example.learningApp.dto.response.order;

import com.example.learningApp.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailResponse {

    private String orderId;
    private String orderCode;
    private Long amount;
    private String paymentMethod;
    private String transactionNo;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    private List<OrderItemResponse> items;
}
