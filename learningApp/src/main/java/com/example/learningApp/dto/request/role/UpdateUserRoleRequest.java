package com.example.learningApp.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "New role name is required")
    private String newRoleName;
}
