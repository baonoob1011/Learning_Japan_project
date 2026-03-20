package com.example.learningApp.dto.response.order;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueSummaryResponse {

    Long totalRevenue;
    Long todayRevenue;
    Long monthRevenue;
    Long totalSuccessOrders;

    LocalDateTime generatedAt;
}
