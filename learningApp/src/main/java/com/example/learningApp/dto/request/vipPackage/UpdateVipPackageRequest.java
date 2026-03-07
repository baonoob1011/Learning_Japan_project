package com.example.learningApp.dto.request.vipPackage;


import com.example.learningApp.enums.PlanType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateVipPackageRequest {

    String name;

    Long price;

    Integer durationDays; // null nếu lifetime

    Boolean active;
}
