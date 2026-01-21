package com.example.learningApp.mapper;

import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.entity.UserVideoTracking;
import com.example.learningApp.repository.UserVideoTrackingRepository;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoProgressMapper {
    UserVideoTracking toUserVideoTracking(VideoProgressRequest request);
}
