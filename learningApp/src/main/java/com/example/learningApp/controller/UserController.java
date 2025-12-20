package com.example.learningApp.controller;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.auth.LogoutRequest;
import com.example.learningApp.dto.request.auth.RefreshTokenRequest;
import com.example.learningApp.dto.request.auth.UserLoginRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.UserLoginResponse;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

}
