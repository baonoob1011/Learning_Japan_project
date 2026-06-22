package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vip_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vip_package_id", nullable = false)
    private VipPackage vipPackage;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime expiredDate;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (this.startDate == null) {
            this.startDate = LocalDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
    }
}

