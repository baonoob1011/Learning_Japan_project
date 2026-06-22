package com.example.learningApp.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderSuccessResponse {

    private String orderId;
    private String orderCode;
    private Long amount;
    private String paymentMethod;
    private String transactionNo;
    private LocalDateTime paidAt;

}

