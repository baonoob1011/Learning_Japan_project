package com.example.learningApp.service.user;

import com.example.learningApp.common.PageResponse;
import com.example.learningApp.dto.request.user.DeleteUsersRequest;
import com.example.learningApp.dto.request.user.UpdateUserRequest;
import com.example.learningApp.dto.response.user.UserChatResponse;
import com.example.learningApp.dto.response.user.UserForAdminResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.dto.response.user.UserStatsResponse;
import com.example.learningApp.entity.Role;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserLearningProgressRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    UserLearningProgressRepository userLearningProgressRepository;

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
    @Transactional
    public UserResponse updateMyProfile(UpdateUserRequest request) {

        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getLevel() != null) {
            user.setLevel(request.getLevel());
        }

        userRepository.save(user);

        return userMapper.toUserResponse(user);
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

    public PageResponse<UserForAdminResponse> getAllUsersManager(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;
        if (search == null) {
            userPage = userRepository.findAll(pageable);
        }else {
            userPage = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    search, search, pageable);
        }

        List<UserForAdminResponse> userForAdminResponse = userPage.getContent().stream().map(this::mapToUserResponse).toList();
        return PageResponse.<UserForAdminResponse>builder()
                .page(page)
                .totalPages(userPage.getTotalPages())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .data(userForAdminResponse)
                .build();
    }

    public PageResponse<UserForAdminResponse> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        // 1. Tìm kiếm và phân trang User
        Page<User> userPage;
        if (search == null) {
            userPage = userRepository.findAll(pageable);
        }else {
            userPage = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    search, search, pageable);
        }

        // 2. Map sang UserResponse và ĐIỀN THÊM DỮ LIỆU HỌC TẬP
        List<UserForAdminResponse> userForAdminResponse = userPage.getContent().stream().map(user -> {
            // Map cơ bản (id, email, name...)
            UserForAdminResponse response = mapToUserResponse(user);

            // Lấy progress mới nhất của user này
            var progress = userLearningProgressRepository.findFirstByUserIdOrderByLastExamAtDesc(user.getId());

            if (progress != null) {
                response.setLevel(progress.getLevel()); // Set Level

                // Tính % hoàn thành (ví dụ đơn giản)
                int percent = (progress.getTotalQuestionsDone() == 0) ? 0 :
                        (int) ((double) progress.getCorrectQuestions() / progress.getTotalQuestionsDone() * 100);
                response.setProcessPercent(percent);

                // Set Stage (Ví dụ: Nếu làm > 10 bài thi là giai đoạn Exam, ngược lại là Junbi)
                response.setStage(progress.getTotalExamsTaken() > 10 ? "Exam" : "Junbi");
            } else {
                // Mặc định cho user mới chưa học gì
                response.setLevel("N5");
                response.setProcessPercent(0);
                response.setStage("Newbie");
            }

            // Set Premium (giả sử User entity có field này hoặc check role)
            // response.setPremium(user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_PREMIUM")));

            return response;
        }).collect(Collectors.toList());

        return PageResponse.<UserForAdminResponse>builder()
                .page(page)
                .totalPages(userPage.getTotalPages())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .data(userForAdminResponse)
                .build();
    }

    // Helper map cơ bản
    private UserForAdminResponse mapToUserResponse(User user) {
        return UserForAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.getEnabled())
                .role(String.valueOf(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList())))
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public void banUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);

        try {
            // Disable user in Cognito
            cognitoClient.adminDisableUser(AdminDisableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ban user failed", e);
        }
    }

    @Transactional
    public void unbanUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        try {
            // Enable user in Cognito
            cognitoClient.adminEnableUser(AdminEnableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Unban user failed", e);
        }
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);

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

    public void deleteUsers(DeleteUsersRequest request) {
        for (String email : request.getEmails()) {
            try {
                // Tái sử dụng hàm delete đơn lẻ ở trên
                deleteUser(email);
            } catch (Exception e) {
                // Log lỗi nếu cần, nhưng tiếp tục xóa người tiếp theo
                System.err.println("Lỗi khi xóa user " + email + ": " + e.getMessage());
                // Tùy nghiệp vụ: Bạn có thể throw lỗi để dừng hẳn hoặc bỏ qua để xóa tiếp
            }
        }
    }
    public UserChatResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserChatResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }


    // ============== SEARCH USER FOR CHAT =================
    public List<UserChatResponse> searchUsersForChat(String keyword) {

        var context = SecurityContextHolder.getContext();
        String currentUserId = context.getAuthentication().getName();

        Pageable pageable = PageRequest.of(0, 20); // giới hạn 20 user

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndEnabledTrue(
                        keyword, keyword, pageable
                );

        return users.stream()
                .filter(user -> !user.getId().equals(currentUserId)) // không hiện chính mình
                .map(user -> UserChatResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .build()
                )
                .toList();
    }

    // UserService.java
    public UserStatsResponse getUserStats() {
        Object result = userRepository.getUserStatisticsRaw();

        // Vì query aggregate trả về 1 dòng duy nhất nên ta ép kiểu sang Object[]
        if (result instanceof Object[] row) {

            // Cách an toàn nhất là ép về Number rồi lấy longValue().
            long total = ((Number) row[0]).longValue();
            long active = ((Number) row[1]).longValue();
            long banned = ((Number) row[2]).longValue();

            return new UserStatsResponse(total, active, banned);
        }

        return new UserStatsResponse(0, 0, 0);
    }

    //============== USER ==================
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        User user = userRepository.findById(userId).orElseThrow();
        return userMapper.toUserResponse(user);
    }

    public void updateAvatarUrl(String url) {

        // lấy userid từ token
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
