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
    @Value("${aws.iam.user-pool-id}")
    String userPoolId;

    public void createRole(CreateRoleRequest request) {

        String roleName = request.getRoleName();

        // 1️⃣ Nếu DB đã có → DONE
        if (roleRepository.existsByRoleName(roleName)) {
            return;
        }

        // 2️⃣ Ensure Cognito group (có thì bỏ qua)
        try {
            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .userPoolId(userPoolId)
                            .groupName(roleName)
                            .build()
            );
        } catch (GroupExistsException ex) {
            log.info("♻ Cognito group '{}' already exists, syncing DB only", roleName);
        }

        // 3️⃣ Tạo DB role (luôn làm nếu DB chưa có)
        Role role = Role.builder()
                .roleName(roleName)
                .build();

        roleRepository.save(role);

        log.info("✅ Role '{}' ensured in DB", roleName);
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
            throw new IllegalStateException("User already has this role");
        }

        user.getRoles().add(role);
        userRepository.save(user);
    }
}
