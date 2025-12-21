package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ExamSectionMapper {
    ExamSection toExamSection(CreateSectionRequest createSectionRequest);
    SectionResponse toExamSectionResponse(ExamSection exam);

}
