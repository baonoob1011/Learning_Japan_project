package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.video.YoutubeTranscriptRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.dto.response.video.YoutubeTranscriptResponse;
import com.example.learningApp.entity.Question;
import com.example.learningApp.entity.YoutubeTranscript;
import com.example.learningApp.entity.YoutubeVideo;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;


@Mapper(componentModel = "spring")
public interface YoutubeTranscriptMapper {

}
