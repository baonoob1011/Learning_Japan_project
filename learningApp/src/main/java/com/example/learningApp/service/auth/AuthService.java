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
    @NonFinal
    @Value("${aws.iam.access-key}")
    String accessKey;
    @NonFinal
    @Value("${aws.iam.secret-key}")
    String secretKey;
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
            // 1️⃣ Create user in Cognito
            AdminCreateUserResponse createResponse = cognitoClient.adminCreateUser(
                    AdminCreateUserRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .userAttributes(
                                    AttributeType.builder().name("email").value(email).build(),
                                    AttributeType.builder().name("email_verified").value("true").build(),
                                    AttributeType.builder().name("name").value(request.getFullName()).build()
                            )
                            .messageAction(MessageActionType.SUPPRESS)
                            .build()
            );
            cognitoUserCreated = true;

            // Lấy sub từ Cognito
            String sub = createResponse.user().attributes().stream()
                    .filter(attr -> "sub".equals(attr.name()))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new IllegalStateException("Cognito user sub not found"));

            // 2️⃣ Set password
            cognitoClient.adminSetUserPassword(
                    AdminSetUserPasswordRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .password(request.getPassword())
                            .permanent(true)
                            .build()
            );

            // 3️⃣ Save DB, dùng sub làm ID
            User user = User.builder()
                    .id(sub)               // <-- lưu sub từ Cognito
                    .email(email)
                    .fullName(request.getFullName())
                    .enabled(true)
                    .build();

            User savedUser = userRepository.save(user);

            // 4️⃣ Assign default role
            if (!isAdmin) {
                roleService.assignRoleToUser(
                        new AssignRoleRequest(savedUser.getId(), "USER")
                );
            }

            // 5️⃣ Success
            chatRoomCommandService.addUserToCommunity(savedUser);
            return userMapper.toUserResponse(savedUser);

        } catch (Exception ex) {

            // 🔥 Manual rollback Cognito
            if (cognitoUserCreated) {
                try {
                    cognitoClient.adminDeleteUser(
                            AdminDeleteUserRequest.builder()
                                    .userPoolId(userPoolId)
                                    .username(email)
                                    .build()
                    );
                    log.warn("♻ Rolled back Cognito user: {}", email);
                } catch (Exception deleteEx) {
                    log.error("❌ Failed to rollback Cognito user {}", email, deleteEx);
                }
            }

            throw new RuntimeException("User registration failed", ex);
        }
    }


    public UserLoginResponse login(UserLoginRequest request) {
        try {
            String email = request.getEmail();

            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", request.getPassword());

            if (clientSecret != null && !clientSecret.isBlank()) {
                String secretHash = CognitoSecretHashUtil
                        .calculateSecretHash(email, clientId, clientSecret);
                authParams.put("SECRET_HASH", secretHash);
            }

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse response =
                    cognitoClient.adminInitiateAuth(authRequest);

            if (response.authenticationResult() == null) {
                throw new RuntimeException("Invalid login credentials");
            }

            return UserLoginResponse.builder()
                    .accessToken(response.authenticationResult().accessToken())
                    .refreshToken(response.authenticationResult().refreshToken())
                    .build();

        } catch (NotAuthorizedException ex) {
            // Sai email hoặc password
            throw new RuntimeException("Invalid email or password");

        } catch (UserNotFoundException ex) {
            throw new RuntimeException("User does not exist");

        } catch (Exception ex) {
            throw new RuntimeException("Login failed", ex);
        }
    }


    public UserLoginResponse refreshToken(String username, String refreshToken) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);

            if (clientSecret != null && !clientSecret.isBlank()) {
                String secretHash = CognitoSecretHashUtil
                        .calculateSecretHash(username, clientId, clientSecret);
                authParams.put("SECRET_HASH", secretHash);
            }

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse response =
                    cognitoClient.adminInitiateAuth(authRequest);

            if (response.authenticationResult() == null ||
                    response.authenticationResult().accessToken() == null) {
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


    public void logout(String accessToken) {
        try {
            // Global sign out (invalidate access token)
            if (accessToken != null && !accessToken.isBlank()) {
                GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
                        .accessToken(accessToken)
                        .build();

                cognitoClient.globalSignOut(signOutRequest);
            }

        } catch (NotAuthorizedException ex) {
            // Token đã hết hạn / đã bị revoke
            throw new RuntimeException("Session already expired");

        } catch (Exception ex) {
            throw new RuntimeException("Logout failed", ex);
        }
    }

}

