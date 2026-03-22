package com.example.learningApp.controller.role;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.role.AssignRoleRequest;
import com.example.learningApp.dto.request.role.UpdateUserRoleRequest;
import com.example.learningApp.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    RoleService roleService;

    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @RequestBody @Valid UpdateUserRoleRequest request) {

        roleService.updateUserRole(request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", null));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @RequestBody @Valid AssignRoleRequest request) {

        roleService.assignRoleToUser(request);
        return ResponseEntity.ok(ApiResponse.success("Role assigned to user", null));
    }
}

