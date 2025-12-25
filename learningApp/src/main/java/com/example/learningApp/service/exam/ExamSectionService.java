package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.mapper.ExamMapper;
import com.example.learningApp.mapper.ExamSectionMapper;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamSectionService {

    ExamRepository examRepository;
    ExamSectionMapper examSectionMapper;
    ExamSectionRepository examSectionRepository;


    public SectionResponse createSection(CreateSectionRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamSection examSection = examSectionRepository.save(examSectionMapper.toExamSection(request));
        return examSectionMapper.toExamSectionResponse(examSection);
    }


}
