package com.example.learningApp.service.user;

import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static com.example.learningApp.configuration.CognitoSecretHashUtil.calculateSecretHash;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    private final UserMapper userMapper;
    @NonFinal
    @Value("${aws.cognito.client-id}")
    String clientId;
    @NonFinal
    @Value("${aws.cognito.client-secret}")
    String clientSecret;
    @NonFinal
    @Value("${aws.cognito.user-pool-id}")
    String userPoolId;

    CognitoIdentityProviderClient cognitoClient;
    UserRepository userRepository;

    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        try{
            ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                    .accessToken(accessToken)
                    .previousPassword(oldPassword)
                    .proposedPassword(newPassword)
                    .build();

            // Call Cognito to change the password
            cognitoClient.changePassword(changePasswordRequest);
        }catch (NotAuthorizedException e) {
            throw new RuntimeException("Old password is incorrect");
        }catch (InvalidPasswordException e) {
            throw new RuntimeException("New password does not meet security policy");
        }catch (Exception ex) {
            throw new RuntimeException("Change password failed", ex);
        }
    }

    public void forgetPassword(String email) {
        // Implementation for forget password
        try{
            ForgotPasswordRequest forgotPasswordRequest = ForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .secretHash(calculateSecretHash(email, clientId, clientSecret))
                    .build();
            cognitoClient.forgotPassword(forgotPasswordRequest);
        }catch (UserNotFoundException e) {
            log.warn("Forgot password requested for non-existing user");
        }catch (Exception e) {
            throw new RuntimeException("Forget password failed", e);
        }
    }

    public void confirmForgotPassword(String email, String confirmationCode, String newPassword) {
        try{
            ConfirmForgotPasswordRequest confirmForgotPasswordRequest = ConfirmForgotPasswordRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .password(newPassword)
                    .secretHash(calculateSecretHash(email, clientId, clientSecret))
                    .build();
            cognitoClient.confirmForgotPassword(confirmForgotPasswordRequest);
        } catch (CodeMismatchException e) {
            throw new RuntimeException("Invalid confirmation code");
        } catch (ExpiredCodeException e) {
            throw new RuntimeException("Confirmation code expired");
        } catch (InvalidPasswordException e) {
            throw new RuntimeException("Password does not meet policy");
        } catch (Exception e) {
            throw new RuntimeException("Confirm forgot password failed", e);
        }
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size, String search) {
        // Implementation for retrieving all users with pagination
        Pageable pageable = PageRequest.of(page - 1,size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (search == null) {
            userPage = userRepository.findAll(pageable);
        }else {
            userPage = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    search, search, pageable);
        }

        List<UserResponse> userResponses = userPage.getContent().stream().map(userMapper::toUserResponse).toList();

        return PageResponse.<UserResponse>builder()
                .data(userResponses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    public void deleteUser(String email) {
        try {
            // Xoá user trong Cognito
            cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Delete user failed", e);
        }
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        User user = userRepository.findById(userId).orElseThrow();
        return userMapper.toUserResponse(user);
    }

    public void updateAvatarUrl(String url) {

        // lấy email từ token
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        // tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // cập nhật avatar
        user.setAvatarUrl(url);

        // lưu lại
        userRepository.save(user);
    }

}
