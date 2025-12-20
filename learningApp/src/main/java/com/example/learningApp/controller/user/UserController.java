package com.example.learningApp.controller.user;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.service.auth.AuthService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse response = authService.registerUser(request,false);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

}
