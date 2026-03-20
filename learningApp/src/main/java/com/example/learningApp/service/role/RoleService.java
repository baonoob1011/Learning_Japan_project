package com.example.learningApp.service.role;

import com.example.learningApp.dto.request.role.AssignRoleRequest;
import com.example.learningApp.dto.request.role.CreateRoleRequest;
import com.example.learningApp.entity.Role;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.RoleRepository;
import com.example.learningApp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupExistsException;

import java.util.HashSet;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    UserRepository userRepository;
    CognitoIdentityProviderClient cognitoClient;

    @NonFinal
    @Value("${aws.cognito.user-pool-id}")
    String userPoolId;

    @Transactional
    public void createRoleIfNotExists(String roleName) {

        // 1️⃣ DB đã có → DONE
        if (roleRepository.existsByRoleName(roleName)) {
            log.info("✔ Role '{}' already exists in DB", roleName);
            return;
        }

        // 2️⃣ Ensure Cognito group
        try {
            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .userPoolId(userPoolId)
                            .groupName(roleName)
                            .build()
            );
            log.info("➕ Created Cognito group '{}'", roleName);

        } catch (GroupExistsException ex) {
            log.info("♻ Cognito group '{}' already exists", roleName);
        }

        // 3️⃣ Always ensure DB role
        Role role = Role.builder()
                .roleName(roleName)
                .build();

        roleRepository.save(role);
        log.info("✅ Role '{}' saved in DB", roleName);
    }
    @Transactional
    public void changeUserRole(String userId, String oldRoleName, String newRoleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Role oldRole = roleRepository.findByRoleName(oldRoleName)
                .orElseThrow(() -> new IllegalStateException("Old role not found"));

        Role newRole = roleRepository.findByRoleName(newRoleName)
                .orElseThrow(() -> new IllegalStateException("New role not found"));

        // ===== 1️⃣ Remove old role in Cognito =====
        cognitoClient.adminRemoveUserFromGroup(builder -> builder
                .userPoolId(userPoolId)
                .username(user.getEmail())
                .groupName(oldRoleName)
        );

        // ===== 2️⃣ Add new role in Cognito =====
        cognitoClient.adminAddUserToGroup(builder -> builder
                .userPoolId(userPoolId)
                .username(user.getEmail())
                .groupName(newRoleName)
        );

        // ===== 3️⃣ Update DB =====
        user.getRoles().remove(oldRole);
        user.getRoles().add(newRole);

        userRepository.save(user);
    }

    public void assignRoleToUser(AssignRoleRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new IllegalStateException("Role not found"));

        // 1️⃣ Add user to Cognito group
        AdminAddUserToGroupRequest addUserRequest =
                AdminAddUserToGroupRequest.builder()
                        .userPoolId(userPoolId)
                        .username(user.getEmail()) // email = username
                        .groupName(role.getRoleName())
                        .build();

        cognitoClient.adminAddUserToGroup(addUserRequest);

        // 2️⃣ Add DB join
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (user.getRoles().contains(role)) {
            throw new RuntimeException("User already has this role");
        }

        user.getRoles().add(role);
        userRepository.save(user);
    }
}

