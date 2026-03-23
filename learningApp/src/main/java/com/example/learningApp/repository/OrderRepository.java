package com.example.learningApp.repository;

import com.example.learningApp.entity.Order;
import com.example.learningApp.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    /* ===================== REVENUE ===================== */
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("""
                SELECT SUM(o.amount)
                FROM Order o
                WHERE o.status = :status
            """)
    Optional<Long> sumAmountByStatus(@Param("status") PaymentStatus status);

    Optional<Order> findByOrderCodeAndUserId(String orderCode, String userId);
    Optional<Order> findByIdAndUserId(String id, String userId);
    boolean existsByOrderCode(String orderCode);

    @Query("""
                SELECT SUM(o.amount)
                FROM Order o
                WHERE o.status = :status
                AND o.paidAt BETWEEN :start AND :end
            """)
    Optional<Long> sumAmountByStatusAndPaidAtBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    Long countByStatus(PaymentStatus status);

    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByStatusInAndCreatedAtBefore(List<PaymentStatus> statuses, LocalDateTime createdAt);

    /* ===================== ANALYTICS ===================== */

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.status = :status
            ORDER BY o.paidAt DESC
            """)
    List<Order> findRecentSuccessOrders(
            @Param("status") PaymentStatus status);

}

