package com.example.learningApp.entity;

import com.example.learningApp.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }
}
