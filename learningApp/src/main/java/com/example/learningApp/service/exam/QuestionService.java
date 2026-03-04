package com.example.learningApp.service.exam;

import com.example.learningApp.dto.request.exam.CreateQuestionRequest;
import com.example.learningApp.dto.request.exam.question.UpdateQuestionRequest;
import com.example.learningApp.dto.response.exam.QuestionResponse;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.mapper.QuestionMapper;
import com.example.learningApp.repository.ExamSectionRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public QuestionResponse updateQuestion(String questionId, UpdateQuestionRequest request) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // nếu đổi section
        if (request.getSectionId() != null) {
            ExamSection section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Section not found"));
            question.setSection(section);
        }

        // update field != null
        questionMapper.updateQuestion(question, request);

        return questionMapper.toQuestionResponse(
                questionRepository.save(question));
    }

    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found"));
        return questionMapper.toQuestionResponse(questionRepository.save(questionMapper.toQuestion(request)));
    }

    public List<QuestionResponse> getQuestionsByExamId(String examId) {
        return questionRepository.findAllByExamId(examId).stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

}
