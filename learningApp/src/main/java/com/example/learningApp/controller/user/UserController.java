package com.example.learningApp.controller.user;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.user.ChangePasswordRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.request.user.ForgotPasswordRequest;
import com.example.learningApp.dto.request.user.UpdateUserRequest;
import com.example.learningApp.dto.response.user.UserChatResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.service.auth.AuthService;
import com.example.learningApp.service.user.AvatarService;
import com.example.learningApp.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    AuthService authService;
    UserService userService;
    AvatarService avatarService;


//    @PostMapping("/online")


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
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestBody @Valid UpdateUserRequest request) {

        UserResponse response = userService.updateMyProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response)
        );
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
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserChatResponse>>> searchUsers(
            @RequestParam String keyword
    ) {

        List<UserChatResponse> response = userService.searchUsersForChat(keyword);

        return ResponseEntity.ok(
                ApiResponse.success("Users found", response)
        );
    }
    @GetMapping("/chat/{id}")
    public ResponseEntity<ApiResponse<UserChatResponse>> getUserById(
            @PathVariable String id
    ) {
        UserChatResponse response = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", response)
        );
    }
}

