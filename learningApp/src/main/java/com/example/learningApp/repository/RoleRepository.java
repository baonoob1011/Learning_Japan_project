package com.example.learningApp.repository;

import com.example.learningApp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);

}
