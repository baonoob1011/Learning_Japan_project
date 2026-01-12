package com.example.learningApp.controller.user;

import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    UserService userService;

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')") // Nên chặn quyền nếu cần
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ){
        PageResponse<UserResponse> response = userService.getAllUsers(page, size, search);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));

    }

    @DeleteMapping("/delete-account")
// @PreAuthorize("hasRole('ADMIN')") // Nên chặn quyền nếu cần
    public ResponseEntity<ApiResponse<String>> deleteAccount(@RequestParam String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(ApiResponse.success("Account deleted permanently", null));
    }
}
