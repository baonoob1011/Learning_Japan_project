package com.example.learningApp.controller.order;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.order.OrderDetailResponse;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.service.order.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserOrderController {

    OrderService orderService;

    /* ===================== GET MY ORDERS ===================== */

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderDetailResponse>>> getMyOrders() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "User orders retrieved successfully",
                        orderService.getMyOrders()
                )
        );
    }

    /* ===================== GET ORDER DETAIL ===================== */

    @GetMapping("/me/{orderCode}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(
            @PathVariable String orderCode
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order detail retrieved successfully",
                        orderService.getOrderDetail(orderCode)
                )
        );
    }
}
