package com.example.learningApp.service;

import com.example.learningApp.configuration.CognitoSecretHashUtil;
import com.example.learningApp.dto.request.auth.UserLoginRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.UserLoginResponse;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.mapper.UserMapper;
import com.example.learningApp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @NonFinal
    @Value("${aws.iam.access-key-cognito}")
    String accessKey;
    @NonFinal
    @Value("${aws.iam.secret-key-cognito}")
    String secretKey;
    @NonFinal
    @Value("${aws.iam.cognito-domain}")
    String cognitoDomain;
    @NonFinal
    @Value("${aws.iam.region-cognito}")
    String region;
    @NonFinal
    @Value("${aws.iam.client-id}")
    String clientId;
    @NonFinal
    @Value("${aws.iam.client-secret}")
    String clientSecret;
    @NonFinal
    @Value("${aws.iam.user-pool-id}")
    String userPoolId;
    public UserResponse registerUser(CreateUserRequest request) {

        // 🔹 Check email đã tồn tại DB
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String email = request.getEmail(); // email sẽ là username trong Cognito

        // 🔹 1. Tạo user trên Cognito
        AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email) // email làm username
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("email_verified").value("true").build(),
                        AttributeType.builder().name("name").value(request.getFullName()).build()
                )
                .messageAction(MessageActionType.SUPPRESS) // Không gửi email tự động
                .build();

        cognitoClient.adminCreateUser(createUserRequest);

        // 🔹 2. Set permanent password do client gửi
        AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .password(request.getPassword())
                .permanent(true)
                .build();

        cognitoClient.adminSetUserPassword(setPasswordRequest);

        // 🔹 3. Lưu user vào DB
        User user = User.builder()
                .email(email)
                .fullName(request.getFullName())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // 🔹 4. Map entity → response
        return userMapper.toUserResponse(savedUser);
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
                throw new IllegalStateException("Invalid login credentials");
            }

            return UserLoginResponse.builder()
                    .accessToken(response.authenticationResult().accessToken())
                    .refreshToken(response.authenticationResult().refreshToken())
                    .build();

        } catch (NotAuthorizedException ex) {
            // Sai email hoặc password
            throw new IllegalStateException("Invalid email or password");

        } catch (UserNotFoundException ex) {
            throw new IllegalStateException("User does not exist");

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

            // Refresh token không hợp lệ / hết hạn / bị revoke
            throw new IllegalStateException("Session expired. Please login again");

        } catch (Exception ex) {
            throw new RuntimeException("Refresh token failed", ex);
        }
    }


    public void logout(String username, String accessToken, String refreshToken) {
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
            throw new IllegalStateException("Session already expired");

        } catch (Exception ex) {
            throw new RuntimeException("Logout failed", ex);
        }
    }



}
