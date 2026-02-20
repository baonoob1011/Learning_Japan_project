package com.example.learningApp.entity;

import com.example.learningApp.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType planType;   // MONTHLY / YEARLY / LIFETIME

    private String name;

    private Long price; // VND

    private Integer durationDays; // số ngày hiệu lực (null = vĩnh viễn)

    private Boolean active;
}
