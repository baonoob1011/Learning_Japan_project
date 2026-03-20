package com.example.learningApp.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YearlyRevenueResponse {
    int year;
    Long revenue;
}

