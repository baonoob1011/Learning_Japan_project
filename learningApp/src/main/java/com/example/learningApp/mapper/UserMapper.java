package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.UserRequest;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface UserMapper {
    com.example.learningApp.entity.User toUser(UserRequest request);


    UserResponse toUserResponse(User user);


}
