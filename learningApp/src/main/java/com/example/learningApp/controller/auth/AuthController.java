package com.example.learningApp.controller.auth;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.auth.LogoutRequest;
import com.example.learningApp.dto.request.auth.RefreshTokenRequest;
import com.example.learningApp.dto.request.auth.UserLoginRequest;
import com.example.learningApp.dto.response.user.UserLoginResponse;
import com.example.learningApp.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<UserLoginResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        UserLoginResponse response = authService.refreshToken(request.getUsername(), request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.success("Refresh token successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));

    }
}
