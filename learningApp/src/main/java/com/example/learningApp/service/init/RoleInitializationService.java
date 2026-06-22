package com.example.learningApp.service.init;

import com.example.learningApp.common.RoleConstants;
import com.example.learningApp.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleInitializationService {

    private final RoleService roleService;

    public void initDefaultRoles() {
        RoleConstants.DEFAULT_ROLES
                .forEach(roleService::createRoleIfNotExists);
    }
}

