package com.example.learningApp.dto.response.vipPackage;


import com.example.learningApp.enums.PlanType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VipPackageResponse {

    String id;
    PlanType planType;
    String name;
    Long price;
    Integer durationDays;
    Boolean active;
}
