package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.entity.Exam;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ExamMapper {
    Exam toExam(CreateExamRequest createExamRequest);
    ExamResponse toExamResponse(Exam exam);

}
