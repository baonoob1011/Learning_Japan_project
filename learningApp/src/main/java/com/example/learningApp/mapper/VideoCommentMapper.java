package com.example.learningApp.mapper;

import com.example.learningApp.dto.response.video.comment.VideoCommentResponse;
import com.example.learningApp.entity.VideoComment;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
@Mapper(componentModel = "spring")
public interface VideoCommentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "userRating", source = "comment", qualifiedByName = "mapUserRating")
    VideoCommentResponse toResponse(
            VideoComment comment,
            @Context Map<String, Integer> ratingMap
    );

    List<VideoCommentResponse> toResponseList(
            List<VideoComment> comments,
            @Context Map<String, Integer> ratingMap
    );

    @Named("mapUserRating")
    default Integer mapUserRating(
            VideoComment comment,
            @Context Map<String, Integer> ratingMap
    ) {
        if (ratingMap == null || comment.getUser() == null) {
            return null;
        }
        return ratingMap.get(comment.getUser().getId());
    }
}
