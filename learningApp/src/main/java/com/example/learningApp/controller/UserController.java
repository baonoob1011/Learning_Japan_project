package com.example.learningApp.controller;

import com.example.learningApp.dto.ApiResponse;
import com.example.learningApp.dto.request.UserRequest;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid UserRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }
}
