package com.example.learningApp.controller.revenue;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.response.order.RevenueSummaryResponse;
import com.example.learningApp.service.order.RevenueService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminRevenueController {

    RevenueService revenueService;

    /* ===================== SUMMARY ===================== */

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenueSummary() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Revenue summary retrieved successfully",
                        revenueService.getRevenueSummary()
                )
        );
    }

    /* ===================== REVENUE BY DAY ===================== */

    @GetMapping("/day")
    public ResponseEntity<ApiResponse<Long>> getRevenueByDay(
            @RequestParam String date
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Revenue by day retrieved successfully",
                        revenueService.getRevenueByDate(LocalDate.parse(date))
                )
        );
    }

    /* ===================== REVENUE BY MONTH ===================== */

    @GetMapping("/month")
    public ResponseEntity<ApiResponse<Long>> getRevenueByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Revenue by month retrieved successfully",
                        revenueService.getRevenueByMonth(year, month)
                )
        );
    }

    /* ===================== TOTAL SUCCESS ORDERS ===================== */

    @GetMapping("/success-count")
    public ResponseEntity<ApiResponse<Long>> countSuccessOrders() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Total success orders retrieved successfully",
                        revenueService.countSuccessOrders()
                )
        );
    }
}