package com.example.learningApp.service.init;

import com.example.learningApp.common.RoleConstants;
import com.example.learningApp.dto.request.role.AssignRoleRequest;
import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.entity.Role;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.RoleRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.auth.AuthService;
import com.example.learningApp.service.role.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class AdminInitializationService {

    private final AuthService authService;
    private final RoleService roleService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.user-pool-id}")
    String userPoolId;

    @Value("${app.admin.email}")
    String email;

    @Value("${app.admin.full-name}")
    String fullName;

    @Value("${app.admin.password}")
    String password;

    @Transactional
    public void initAdminUser() {

        User admin = ensureAdminUser();
        ensureAdminRoleAssigned(admin);
    }

    private User ensureAdminUser() {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    if (cognitoUserExists(email)) {
                        String sub = getCognitoSub(email);
                        return userRepository.save(
                                User.builder()
                                        .id(sub)
                                        .email(email)
                                        .fullName(fullName)
                                        .enabled(true)
                                        .build()
                        );
                    }

                    CreateUserRequest request = new CreateUserRequest();
                    request.setEmail(email);
                    request.setPassword(password);
                    request.setFullName(fullName);

                    authService.registerUser(request, true);
                    return userRepository.findByEmail(email).orElseThrow();
                });
    }

    private void ensureAdminRoleAssigned(User adminUser) {

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN)
                .orElseThrow();

        if (adminUser.getRoles() != null &&
                adminUser.getRoles().contains(adminRole)) {
            return;
        }

        roleService.assignRoleToUser(
                new AssignRoleRequest(adminUser.getId(), RoleConstants.ADMIN)
        );
    }

    private boolean cognitoUserExists(String email) {
        try {
            cognitoClient.adminGetUser(
                    AdminGetUserRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .build()
            );
            return true;
        } catch (UserNotFoundException ex) {
            return false;
        }
    }

    private String getCognitoSub(String email) {
        return cognitoClient.adminGetUser(
                        AdminGetUserRequest.builder()
                                .userPoolId(userPoolId)
                                .username(email)
                                .build()
                ).userAttributes().stream()
                .filter(a -> "sub".equals(a.name()))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow();
    }
}
