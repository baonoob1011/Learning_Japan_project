package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface QuestionMapper {
    Question toQuestion(CreateQuestionRequest createQuestionRequest);

    QuestionResponse toQuestionResponse(Question question);

}
