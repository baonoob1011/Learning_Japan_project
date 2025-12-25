package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.mapper.ExamSectionMapper;
import com.example.learningApp.mapper.QuestionMapper;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionService {

    QuestionRepository questionRepository;
    ExamSectionRepository sectionRepository;
    QuestionMapper questionMapper;

    public List<QuestionResponse> getQuestionsBySection(String sectionId) {
        List<Question> questions = questionRepository.findBySectionId(sectionId);
        // Map bằng mapper

        return questions.stream()

                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    public List<QuestionResponse> getAll() {
        return questionRepository.findAll()
                .stream()
                // ✅ sort đúng: sectionOrder -> questionOrder
                .map(q -> {
                    QuestionResponse res = new QuestionResponse();

                    res.setId(q.getId());
                    // ⭐ LẤY sectionOrder TỪ SECTION
                    res.setSectionOrder(q.getSection().getSectionOrder());

                    res.setQuestionType(q.getQuestionType());
                    res.setQuestionText(q.getQuestionText());
                    res.setOptions(q.getOptions());
                    res.setAnswer(q.getAnswer());
                    res.setImageUrl(q.getImageUrl());
                    res.setAudioUrl(q.getAudioUrl());
                    res.setQuestionOrder(q.getQuestionOrder());

                    return res;
                })
                .toList();
    }


    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        ExamSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));
        return questionMapper.toQuestionResponse(questionRepository.save(questionMapper.toQuestion(request)));
    }

}
