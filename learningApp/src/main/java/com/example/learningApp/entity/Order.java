package com.example.learningApp.entity;

import com.example.learningApp.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /* ===================== RELATION ===================== */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vip_package_id", nullable = false)
    private VipPackage vipPackage;

    /* ===================== PAYMENT INFO ===================== */

    @Column(nullable = false)
    private Long amount; // VND

    @Column(nullable = false, unique = true)
    private String orderCode; // vnp_TxnRef

    private String transactionNo; // vnp_TransactionNo

    private String paymentMethod; // VNPAY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // PENDING / SUCCESS / FAILED

    /* ===================== TIME ===================== */

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime expiredAt;

    /* ===================== AUTO ===================== */

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }
}
