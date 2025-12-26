package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.video.YoutubeVideoResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.YoutubeVideo;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface YoutubeVideoMapper {
  YoutubeVideoResponse toYoutubeVideoResponse(YoutubeVideo youtubeVideo);

}
