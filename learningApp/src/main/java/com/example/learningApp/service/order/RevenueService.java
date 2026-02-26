package com.example.learningApp.service.order;


import com.example.learningApp.dto.response.order.RevenueSummaryResponse;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final OrderRepository orderRepository;

    /* ===================== TOTAL REVENUE ===================== */

    public Long getTotalRevenue() {
        return orderRepository.sumAmountByStatus(PaymentStatus.SUCCESS)
                .orElse(0L);
    }

    /* ===================== REVENUE BY DAY ===================== */

    public Long getRevenueByDate(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return orderRepository.sumAmountByStatusAndPaidAtBetween(
                PaymentStatus.SUCCESS,
                start,
                end
        ).orElse(0L);
    }

    /* ===================== REVENUE BY MONTH ===================== */

    public Long getRevenueByMonth(int year, int month) {

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return orderRepository.sumAmountByStatusAndPaidAtBetween(
                PaymentStatus.SUCCESS,
                start,
                end
        ).orElse(0L);
    }

    public Long countSuccessOrders() {
        return orderRepository.countByStatus(PaymentStatus.SUCCESS);
    }
    public RevenueSummaryResponse getRevenueSummary() {

        Long total = getTotalRevenue();
        Long today = getRevenueByDate(LocalDate.now());
        Long month = getRevenueByMonth(
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue()
        );
        Long count = countSuccessOrders();

        return RevenueSummaryResponse.builder()
                .totalRevenue(total)
                .todayRevenue(today)
                .monthRevenue(month)
                .totalSuccessOrders(count)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}