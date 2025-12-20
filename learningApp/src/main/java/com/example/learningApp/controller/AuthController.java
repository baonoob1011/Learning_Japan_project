package com.example.learningApp.controller;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.RefreshTokenRequest;
import com.example.learningApp.dto.request.UserLoginRequest;
import com.example.learningApp.dto.request.UserRequest;
import com.example.learningApp.dto.response.UserLoginResponse;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

    AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid UserRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", userService.login(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<UserLoginResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        UserLoginResponse response = userService.refreshToken(request.getUsername(),request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.success("Refresh token successful", response)
        );
    }

}
