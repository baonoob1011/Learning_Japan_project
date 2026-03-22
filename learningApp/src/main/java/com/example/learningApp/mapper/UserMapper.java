package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.user.UserResponse;
import com.example.learningApp.entity.Role;
import com.example.learningApp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserRequest request);
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    @Mapping(target = "isPremium", expression = "java(isPremium(user))")
    UserResponse toUserResponse(User user);

    default List<String> mapRoles(User user) {
        if (user == null || user.getRoles() == null) {
            return Collections.emptyList();
        }
        return user.getRoles().stream()
                .map(Role::getRoleName)
                .distinct()
                .collect(Collectors.toList());
    }

    default Boolean isPremium(User user) {
        if (user == null) {
            return false;
        }
        boolean hasVipRole = user.getRoles() != null
                && user.getRoles().stream().anyMatch(r -> "USER_VIP".equalsIgnoreCase(r.getRoleName()));
        LocalDateTime now = LocalDateTime.now();
        boolean vipByDate = user.getVipExpiredAt() != null && user.getVipExpiredAt().isAfter(now);
        return hasVipRole || vipByDate;
    }

}

