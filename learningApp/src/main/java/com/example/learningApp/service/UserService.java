package com.example.learningApp.service;

import com.example.learningApp.configuration.CognitoSecretHashUtil;
import com.example.learningApp.dto.request.UserLoginRequest;
import com.example.learningApp.dto.request.UserRequest;
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
public class UserService {

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
    public UserResponse registerUser(UserRequest request) {

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
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", request.getEmail());
            authParams.put("PASSWORD", request.getPassword());

            // Nếu client secret có, thêm SECRET_HASH
            String secretHash = CognitoSecretHashUtil.calculateSecretHash(request.getEmail(), clientId, clientSecret);
            if (secretHash != null) {
                authParams.put("SECRET_HASH", secretHash);
            }

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(authRequest);

            // Lấy token từ Cognito response
            String accessToken = response.authenticationResult().accessToken();
            String refreshToken = response.authenticationResult().refreshToken();

            return UserLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

}
