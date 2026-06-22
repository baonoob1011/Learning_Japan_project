package com.example.learningApp.service.order;

import com.example.learningApp.dto.response.order.*;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.repository.OrderRepository;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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
                end).orElse(0L);
    }

    /* ===================== REVENUE BY MONTH ===================== */

    public Long getRevenueByMonth(int year, int month) {

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return orderRepository.sumAmountByStatusAndPaidAtBetween(
                PaymentStatus.SUCCESS,
                start,
                end).orElse(0L);
    }

    public Long countSuccessOrders() {
        return orderRepository.countByStatus(PaymentStatus.SUCCESS);
    }

    public RevenueSummaryResponse getRevenueSummary() {

        Long total = getTotalRevenue();
        Long today = getRevenueByDate(LocalDate.now());
        Long month = getRevenueByMonth(
                LocalDate.now().getYear(),
                LocalDate.now().getMonthValue());
        Long count = countSuccessOrders();

        return RevenueSummaryResponse.builder()
                .totalRevenue(total)
                .todayRevenue(today)
                .monthRevenue(month)
                .totalSuccessOrders(count)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /* ===================== NEW ANALYTICS ===================== */

    public List<ProductRevenueResponse> getRevenueByProductType() {
        List<Order> orders = orderRepository.findRecentSuccessOrders(PaymentStatus.SUCCESS);
        Map<String, ProductRevenueResponse> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getCourse() != null) {
                    String id = item.getCourse().getId();
                    String title = item.getCourse().getTitle();
                    productMap.merge(id,
                            ProductRevenueResponse.builder()
                                    .productId(id).name(title).type("COURSE").revenue(item.getPrice()).build(),
                            (old, val) -> {
                                old.setRevenue(old.getRevenue() + val.getRevenue());
                                return old;
                            });
                } else if (item.getVipPackage() != null) {
                    String id = item.getVipPackage().getId();
                    String name = "VIP - " + item.getVipPackage().getName();
                    productMap.merge(id,
                            ProductRevenueResponse.builder()
                                    .productId(id).name(name).type("VIP").revenue(item.getPrice()).build(),
                            (old, val) -> {
                                old.setRevenue(old.getRevenue() + val.getRevenue());
                                return old;
                            });
                }
            }
        }

        return productMap.values().stream()
                .sorted(Comparator.comparing(ProductRevenueResponse::getRevenue).reversed())
                .collect(Collectors.toList());
    }

    public List<MonthlyRevenueResponse> getRevenueByMonthOfYear(int year) {
        List<MonthlyRevenueResponse> results = new ArrayList<>();
        String[] monthNames = { "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12" };

        for (int m = 1; m <= 12; m++) {
            Long rev = getRevenueByMonth(year, m);
            results.add(new MonthlyRevenueResponse(m, monthNames[m - 1], rev));
        }
        return results;
    }

    public List<YearlyRevenueResponse> getRevenueByYearRange(int startYear, int endYear) {
        List<YearlyRevenueResponse> results = new ArrayList<>();
        for (int y = startYear; y <= endYear; y++) {
            Long total = 0L;
            for (int m = 1; m <= 12; m++) {
                total += getRevenueByMonth(y, m);
            }
            results.add(new YearlyRevenueResponse(y, total));
        }
        return results;
    }

    public List<OrderResponse> getRecentTransactions() {
        List<Order> orders = orderRepository.findRecentSuccessOrders(PaymentStatus.SUCCESS);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    public Map<LocalDate, Long> getRevenueLast30Days() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        List<Order> orders = orderRepository.findRecentSuccessOrders(PaymentStatus.SUCCESS);
        Map<LocalDate, Long> dailyRevenue = new TreeMap<>();

        // Initialize with zeros
        for (int i = 0; i <= 30; i++) {
            dailyRevenue.put(start.plusDays(i), 0L);
        }

        for (Order order : orders) {
            if (order.getPaidAt() != null) {
                LocalDate date = order.getPaidAt().toLocalDate();
                if (date.isAfter(start.minusDays(1)) && date.isBefore(end.plusDays(1))) {
                    dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0L) + order.getAmount());
                }
            }
        }

        return dailyRevenue;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .orderCode(order.getOrderCode())
                .transactionNo(order.getTransactionNo())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .user(UserSimpleResponse.builder()
                        .fullName(order.getUser() != null ? order.getUser().getFullName() : "Unknown")
                        .email(order.getUser() != null ? order.getUser().getEmail() : "N/A")
                        .avatarUrl(order.getUser() != null ? order.getUser().getAvatarUrl() : null)
                        .build())
                .orderItems(
                        order.getOrderItems().stream().map(this::mapToOrderItemResponse).collect(Collectors.toList()))
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productType(item.getProductType())
                .courseId(item.getCourse() != null ? item.getCourse().getId() : null)
                .courseName(item.getCourse() != null ? item.getCourse().getTitle() : null)
                .courseTitle(item.getCourse() != null ? item.getCourse().getTitle() : null)
                .vipPackageId(item.getVipPackage() != null ? item.getVipPackage().getId() : null)
                .vipPackageName(item.getVipPackage() != null ? item.getVipPackage().getName() : null)
                .price(item.getPrice())
                .build();
    }
}
