package com.example.learningApp.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRevenueResponse {
    String productId;
    String name;
    String type; // "COURSE" or "VIP"
    Long revenue;
}

