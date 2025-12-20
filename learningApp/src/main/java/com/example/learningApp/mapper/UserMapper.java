package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.user.CreateUserRequest;
import com.example.learningApp.dto.response.UserResponse;
import com.example.learningApp.entity.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserRequest request);


    UserResponse toUserResponse(User user);


}
