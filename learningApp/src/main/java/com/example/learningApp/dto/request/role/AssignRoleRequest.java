package com.example.learningApp.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignRoleRequest {

    @NotNull
    private String userId;

    @NotBlank
    private String roleName;
}
