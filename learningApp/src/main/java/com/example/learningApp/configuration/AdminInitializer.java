package com.example.learningApp.configuration;

import com.example.learningApp.dto.request.role.AssignRoleRequest;
import com.example.learningApp.dto.request.role.CreateRoleRequest;
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
    @Value("${aws.iam.user-pool-id}")
    String userPoolId;

    @NonFinal
    @Value("${app.admin.email}")
    String email;

    @NonFinal
    @Value("${app.admin.full-name}")
    String fullName;

    @NonFinal
    @Value("${app.admin.role}")
    String role;

    @NonFinal
    @Value("${app.admin.password}")
    String password;

    static String DEFAULT_USER_ROLE = "USER";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info(" Initializing admin user & role...");
        ensureAdminRole();
        User adminUser = ensureAdminUser();
        ensureAdminRoleAssigned(adminUser);

        log.info("Admin initialization completed");
    }

    // =============================

    private void ensureAdminRole() {
        if (roleRepository.existsByRoleName(role)) {
            log.info("✔ Role {} already exists", role);
            return;
        }

        log.info("➕ Creating role {}", role);
        roleService.createRole(new CreateRoleRequest(role));
    }

    private User ensureAdminUser() {

        // Case 1: DB đã có
        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    boolean existsInCognito = cognitoUserExists(email);

                    //  Case 2: Cognito có – DB chưa có
                    if (existsInCognito) {
                        log.info("♻ Cognito user exists but DB missing → creating DB record");

                        User user = User.builder()
                                .email(email)
                                .fullName(fullName)
                                .enabled(true)
                                .build();

                        return userRepository.save(user);
                    }

                    //  Case 3: chưa có ở cả Cognito lẫn DB
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
        Role adminRole = roleRepository.findByRoleName(role)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + role));

        if (adminUser.getRoles() != null &&
                adminUser.getRoles().contains(adminRole)) {
            log.info("✔ Admin already has role {}", role);
            return;
        }

        log.info(" Assigning role {} to admin", role);

        roleService.assignRoleToUser(
                new AssignRoleRequest(adminUser.getId(), role)
        );
    }

    public boolean cognitoUserExists(String email) {
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
