package com.example.learningApp.service.auth;

import com.example.learningApp.configuration.CognitoSecretHashUtil;
import com.example.learningApp.dto.request.auth.UserLoginRequest;
import com.example.learningApp.dto.request.role.AssignRoleRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.user.UserLoginResponse;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.chat.ChatRoomCommandService;
import com.example.learningApp.service.role.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    CognitoIdentityProviderClient cognitoClient;
    UserMapper userMapper;
    RoleService roleService;
    ChatRoomCommandService chatRoomCommandService;
    SessionService sessionService;
    SimpMessagingTemplate messagingTemplate; // <--- Dùng để bắn tín hiệu đá người dùng ngay lập tức
    // <--- Tích hợp SessionService để xử lý Single Session

    @NonFinal
    @Value("${aws.cognito.client-id}")
    String clientId;

    @NonFinal
    @Value("${aws.cognito.client-secret}")
    String clientSecret;

    @NonFinal
    @Value("${aws.cognito.user-pool-id}")
    String userPoolId;

    @Transactional
    public UserResponse registerUser(CreateUserRequest request, boolean isAdmin) {
        String email = request.getEmail();
        boolean cognitoUserCreated = false;
        try {
            // 1. Create user in Cognito
            AdminCreateUserResponse createResponse = cognitoClient.adminCreateUser(
                    AdminCreateUserRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .userAttributes(
                                    AttributeType.builder().name("email").value(email).build(),
                                    AttributeType.builder().name("email_verified").value("true").build(),
                                    AttributeType.builder().name("name").value(request.getFullName()).build())
                            .messageAction(MessageActionType.SUPPRESS)
                            .build());
            cognitoUserCreated = true;

            String sub = createResponse.user().attributes().stream()
                    .filter(attr -> "sub".equals(attr.name()))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new IllegalStateException("Cognito user sub not found"));

            // 2. Set password
            cognitoClient.adminSetUserPassword(
                    AdminSetUserPasswordRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .password(request.getPassword())
                            .permanent(true)
                            .build());

            // 3. Save DB, using sub (ID của Cognito) làm Primary Key cho DB User
            User user = User.builder()
                    .id(sub)
                    .email(email)
                    .fullName(request.getFullName())
                    .enabled(true)
                    .build();

            User savedUser = userRepository.save(user);

            // 4. Assign default role
            if (!isAdmin) {
                roleService.assignRoleToUser(new AssignRoleRequest(savedUser.getId(), "USER"));
            }

            chatRoomCommandService.addUserToCommunity(savedUser);
            return userMapper.toUserResponse(savedUser);

        } catch (Exception ex) {
            if (cognitoUserCreated) {
                try {
                    cognitoClient.adminDeleteUser(
                            AdminDeleteUserRequest.builder().userPoolId(userPoolId).username(email).build());
                    log.warn("♻ Rolled back Cognito user: {}", email);
                } catch (Exception deleteEx) {
                    log.error("❌ Failed to rollback Cognito user {}", email, deleteEx);
                }
            }
            throw new RuntimeException("User registration failed", ex);
        }
    }

    /**
     * Authenticates with Cognito AND initializes Single Session in Redis.
     */
    public UserLoginResponse login(UserLoginRequest request, String deviceInfo, String ipAddress) {
        try {
            String email = request.getEmail();
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", request.getPassword());

            if (clientSecret != null && !clientSecret.isBlank()) {
                String secretHash = CognitoSecretHashUtil.calculateSecretHash(email, clientId, clientSecret);
                authParams.put("SECRET_HASH", secretHash);
            }

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(authRequest);

            if (response.authenticationResult() == null) {
                throw new RuntimeException("Invalid login credentials");
            }

            // 1. Force Global Sign Out (Kick all other devices out of Cognito)
            try {
                cognitoClient.adminUserGlobalSignOut(AdminUserGlobalSignOutRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .build());
                log.info("[AUTH] Global sign-out performed for user {}", email);
            } catch (Exception e) {
                log.warn("Global sign-out failed or user was already signed out: {}", e.getMessage());
            }

            // 2. Get the new Access Token from Cognito Response
            String accessToken = response.authenticationResult().accessToken();

            // 3. Find userId in DB to map to Redis
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User profile not found in database"));

            // 4. [SINGLE SESSION] Overwrite Redis with new SessionId
            String sessionId = sessionService.initNewSession(user.getId(), deviceInfo, ipAddress);

            // 🔥 [INSTANT KICK OUT] Bắn tín hiệu qua WebSocket tới thiết bị A ngay lập tức
            try {
                messagingTemplate.convertAndSend("/topic/user/" + user.getId() + "/kick-out",
                        "ALREADY_LOGGED_IN_ANOTHER_DEVICE");
                log.info("[AUTH] Kick-out signal sent to user {}", user.getId());
            } catch (Exception wsEx) {
                log.warn("Failed to send WebSocket kick-out: {}", wsEx.getMessage());
            }

            return UserLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(response.authenticationResult().refreshToken())
                    .sessionId(sessionId) // <--- Trả về sessionId cho Frontend
                    .build();

        } catch (NotAuthorizedException ex) {
            throw new RuntimeException("Invalid email or password");
        } catch (UserNotFoundException ex) {
            throw new RuntimeException("User does not exist");
        } catch (Exception ex) {
            log.error("Login Error: ", ex);
            throw new RuntimeException("Login failed: " + ex.getMessage());
        }
    }

    public UserLoginResponse refreshToken(String username, String refreshToken) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);

            if (clientSecret != null && !clientSecret.isBlank()) {
                String secretHash = CognitoSecretHashUtil.calculateSecretHash(username, clientId, clientSecret);
                authParams.put("SECRET_HASH", secretHash);
            }

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(authRequest);

            if (response.authenticationResult() == null || response.authenticationResult().accessToken() == null) {
                throw new IllegalStateException("Unable to refresh token");
            }

            return UserLoginResponse.builder()
                    .accessToken(response.authenticationResult().accessToken())
                    .refreshToken(refreshToken)
                    .build();

        } catch (NotAuthorizedException ex) {
            throw new RuntimeException("Session expired. Please login again");
        } catch (Exception ex) {
            throw new RuntimeException("Refresh token failed", ex);
        }
    }

    public void logout(String accessToken, String userId) {
        try {
            if (accessToken != null && !accessToken.isBlank()) {
                GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder().accessToken(accessToken).build();
                cognitoClient.globalSignOut(signOutRequest);
            }
            // 🔥 Xóa Session ID khỏi Redis
            if (userId != null) {
                sessionService.logout(userId);
            }
        } catch (Exception ex) {
            log.warn("Logout warning: {}", ex.getMessage());
        }
    }
}
