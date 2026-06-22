package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.dto.response.video.VideoProgressResponse;
import com.example.learningApp.entity.UserVideoTracking;
import com.example.learningApp.repository.UserVideoTrackingRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoProgressMapper {
    @Mapping(target = "videoId", source = "video.id")
    VideoProgressResponse toVideoProgressResponse(UserVideoTracking tracking);
}

