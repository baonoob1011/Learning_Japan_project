package com.example.learningApp.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSimpleResponse {
    String fullName;
    String email;
    String avatarUrl;
}
