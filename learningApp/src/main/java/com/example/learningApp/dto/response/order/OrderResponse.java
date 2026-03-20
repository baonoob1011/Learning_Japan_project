package com.example.learningApp.dto.response.order;

import com.example.learningApp.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    Long amount;
    String orderCode;
    String transactionNo;
    String paymentMethod;
    PaymentStatus status;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
    UserSimpleResponse user;
    List<OrderItemResponse> orderItems;
}

