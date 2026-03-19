package com.example.learningApp.dto.response.order;

import com.example.learningApp.enums.ProductType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String id;
    ProductType productType;
    String courseId;
    String courseName;
    String courseTitle; // For my new dashboard
    String vipPackageId;
    String vipPackageName;
    Long price;
}