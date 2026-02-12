package com.example.learningApp.controller.user;

import com.example.learningApp.common.PageResponse;
import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.user.DeleteUsersRequest;
import com.example.learningApp.dto.response.user.UserChatResponse;
import com.example.learningApp.dto.response.user.UserForAdminResponse;
import com.example.learningApp.dto.response.user.UserStatsResponse;
import com.example.learningApp.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserForAdminResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ){
        PageResponse<UserForAdminResponse> response = userService.getAllUsers(page, size, search);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));

    }

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<PageResponse<UserForAdminResponse>>> getAllUsersManager(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ){
        PageResponse<UserForAdminResponse> response = userService.getAllUsersManager(page, size, search);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));

    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@RequestParam String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok(ApiResponse.success("Account deleted permanently", null));
    }

    @DeleteMapping("/delete-accounts")
    public ResponseEntity<ApiResponse<String>> deleteManyUsers(@RequestBody DeleteUsersRequest emails) {
        userService.deleteUsers(emails);
        return ResponseEntity.ok(ApiResponse.success("Accounts deleted permanently", null));
    }

    @PostMapping("/ban/{email}")
    public ResponseEntity<ApiResponse<String>> banUser(@PathVariable String email) {
        userService.banUser(email);
        return ResponseEntity.ok(ApiResponse.success("User banned successfully", null));
    }

    @PostMapping("/unban/{email}")
    public ResponseEntity<ApiResponse<String>> unbanUser(@PathVariable String email) {
        userService.unbanUser(email);
        return ResponseEntity.ok(ApiResponse.success("User unbanned successfully", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStatistics() {
        UserStatsResponse response = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", response));
    }

}
