package com.example.learningApp.controller.user;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.user.ChangePasswordRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.request.user.ForgotPasswordRequest;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.service.auth.AuthService;
import com.example.learningApp.service.user.AvatarService;
import com.example.learningApp.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    AuthService authService;
    UserService userService;
    AvatarService avatarService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse response = authService.registerUser(request,false);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("Authorization") String accessToken, @RequestBody ChangePasswordRequest request){
        String token = accessToken.replace("Bearer ", "");
        userService.changePassword(token, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam String email){
        userService.forgetPassword(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent if user exists", null));
    }

    @PostMapping("/confirm-forgot-password")
    public ResponseEntity<ApiResponse<String>> confirmForgotPassword(@RequestBody ForgotPasswordRequest request){
        userService.confirmForgotPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(){
        UserResponse response = userService.getMyInfo();
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @PutMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestPart("file") MultipartFile file) throws IOException {

        String url = avatarService.uploadAvatar(file);

        userService.updateAvatarUrl(url);

        return ResponseEntity.ok(ApiResponse.success("Upload success", url));
    }
}
