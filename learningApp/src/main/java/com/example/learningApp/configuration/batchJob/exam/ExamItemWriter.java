package com.example.learningApp.configuration.batchJob.exam;

import com.example.learningApp.entity.Exam;
import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.entity.Question;
import com.example.learningApp.repository.ExamRepository;
import com.example.learningApp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ExamItemWriter {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Bean(name = "examWriter")
    public ItemWriter<Question> examWriter() {
        return items -> {
            List<Question> list = StreamSupport.stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Set<Exam> exams = list.stream()
                    .map(Question::getSection)
                    .flatMap(s -> s.getExams().stream())
                    .collect(Collectors.toSet());

            for (Exam detachedExam : exams) {
                // Re-fetch managed instance to avoid detached collection issues across chunks
                Exam exam = examRepository.findByCode(detachedExam.getCode())
                        .orElse(detachedExam);

                // Filter items belonging to this exam in current chunk
                List<Question> chunkQuestionsForThisExam = list.stream()
                        .filter(q -> q.getSection().getExams().stream()
                                .anyMatch(e -> e.getCode().equals(exam.getCode())))
                        .collect(Collectors.toList());

                // 1. Sync Sections
                Set<ExamSection> chunkSectionsForThisExam = chunkQuestionsForThisExam.stream()
                        .map(Question::getSection)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (ExamSection s : chunkSectionsForThisExam) {
                    if (!exam.getSections().contains(s)) {
                        System.out.println("--- Adding Section to Exam: " + s.getTitle() + " (Order: "
                                + s.getSectionOrder() + ")");
                        exam.getSections().add(s);
                    }
                }

                // 2. Sync Questions
                for (Question q : chunkQuestionsForThisExam) {
                    if (!exam.getQuestions().contains(q)) {
                        exam.getQuestions().add(q);
                        // Also ensure the back-reference is set if it's a ManyToMany on Question side
                        // too
                        if (!q.getExams().contains(exam)) {
                            q.getExams().add(exam);
                        }
                    }
                }

                // Update counts and save
                exam.setNumSections(exam.getSections().size());
                exam.setNumQuestions(exam.getQuestions().size());
                System.out.println("--- Saving Exam " + exam.getCode() + ": " + exam.getNumSections() + " sections, "
                        + exam.getNumQuestions() + " questions total.");
                Exam savedExam = examRepository.saveAndFlush(exam);

                // Evict Redis cache to ensure UI shows new questions/sections
                redisTemplate.delete("exam:" + savedExam.getId() + ":questions");
                redisTemplate.delete("exam:" + savedExam.getId() + ":sections");
            }

            // Re-save questions to persist the relationship from the Question side if
            // needed
            // (though joining through Exam.save should handle it)
            questionRepository.saveAll(list);
        };
    }
}
