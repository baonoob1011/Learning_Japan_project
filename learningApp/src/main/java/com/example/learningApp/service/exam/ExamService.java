package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.CreateExamRequest;
import com.example.learningApp.dto.response.exam.ExamResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.mapper.ExamMapper;
import com.example.learningApp.repository.ExamRepository;
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
public class ExamService {

    ExamRepository examRepository;
    ExamMapper examMapper;


    public List<ExamResponse> searchExams(String keyword) {
        List<Exam> exams = examRepository.searchByKeyword(keyword);
        return exams.stream()
                .map(examMapper::toExamResponse)
                .collect(Collectors.toList());
    }

    public List<ExamResponse> getAllExams() {
        // Map từ List<Exam> sang List<ExamResponse>
        return examRepository.findAll().stream()
                .map(examMapper::toExamResponse)
                .toList();
    }

    public ExamResponse getExamById(String id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + id));
        return examMapper.toExamResponse(exam);
    }

    public ExamResponse createExam(CreateExamRequest createExamRequest) {
        try {
            // Kiểm tra code đã tồn tại chưa
            boolean exists = examRepository.existsByCode(createExamRequest.getCode());
            if (exists) {
                throw new IllegalArgumentException("Exam code already exists: " + createExamRequest.getCode());
            }

            // Lưu entity
            Exam savedExam = examRepository.save(examMapper.toExam(createExamRequest));

            // Map entity → response
            return examMapper.toExamResponse(savedExam);
        } catch (IllegalArgumentException e) {
            log.error("Validation error while creating exam: {}", e.getMessage());
            throw e;  // có thể ném tiếp để controller xử lý
        } catch (Exception e) {
            log.error("Unexpected error while creating exam", e);
            throw new RuntimeException("Failed to create exam. Please try again later.");
        }
    }


}
