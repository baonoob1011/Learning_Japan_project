package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.progress.UserLearningProgressResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.UserLearningProgress;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserLearningProgressMapper {
UserLearningProgressResponse toUserLearningProgressResponse (UserLearningProgress userLearningProgress);

}

