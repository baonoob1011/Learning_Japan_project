package com.example.learningApp.configuration;

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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminInitializer implements ApplicationRunner {

    AuthService authService;
    RoleService roleService;
    UserRepository userRepository;
    RoleRepository roleRepository;
    CognitoIdentityProviderClient cognitoClient;

    @NonFinal
    @Value("${aws.cognito.user-pool-id}")
    String userPoolId;

    @NonFinal
    @Value("${app.admin.email}")
    String email;

    @NonFinal
    @Value("${app.admin.full-name}")
    String fullName;

    @NonFinal
    @Value("${app.admin.password}")
    String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("🚀 Initializing system defaults...");

        ensureDefaultRoles();          // ✅ BẮT BUỘC
        User adminUser = ensureAdminUser();
        ensureAdminRoleAssigned(adminUser);

        log.info("✅ Admin initialization completed");
    }

    // =========================

    private void ensureDefaultRoles() {
        RoleConstants.DEFAULT_ROLES.forEach(roleService::createRoleIfNotExists);
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
                .orElseThrow(() ->
                        new IllegalStateException("Cognito sub not found"));
    }

    private User ensureAdminUser() {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    if (cognitoUserExists(email)) {
                        log.info("♻ Cognito user exists but DB missing → creating DB record");
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

                    log.info("➕ Creating admin user in Cognito & DB");

                    CreateUserRequest request = new CreateUserRequest();
                    request.setEmail(email);
                    request.setPassword(password);
                    request.setFullName(fullName);

                    authService.registerUser(request, true);

                    return userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new IllegalStateException("Failed to create admin user"));
                });
    }

    private void ensureAdminRoleAssigned(User adminUser) {

        Role adminRole = roleRepository.findByRoleName(RoleConstants.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        if (adminUser.getRoles() != null &&
                adminUser.getRoles().contains(adminRole)) {
            log.info("✔ Admin already has ADMIN role");
            return;
        }

        roleService.assignRoleToUser(
                new AssignRoleRequest(adminUser.getId(), RoleConstants.ADMIN)
        );

        log.info("🔑 ADMIN role assigned to admin user");
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
}
