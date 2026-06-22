package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.feedback.CreateFeedbackRequest;
import com.example.learningApp.dto.response.feedback.FeedbackResponse;
import com.example.learningApp.entity.Feedback;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    // Entity → Response
    @Mapping(source = "user.id", target = "userId")
    FeedbackResponse toResponse(Feedback feedback);

    // Request → Entity (chưa set user, status, createdAt)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    Feedback toEntity(CreateFeedbackRequest request);
}
