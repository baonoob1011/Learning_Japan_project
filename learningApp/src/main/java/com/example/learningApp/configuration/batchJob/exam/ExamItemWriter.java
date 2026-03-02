package com.example.learningApp.configuration.batchJob.exam;

import com.example.learningApp.entity.Exam;
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

    @Bean(name = "examWriter")
    public ItemWriter<Question> examWriter() {
        return items -> {
            List<Question> list = StreamSupport.stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            questionRepository.saveAll(list);

            Set<Exam> exams = list.stream()
                    .flatMap(q -> q.getExams().stream())
                    .collect(Collectors.toSet());

            for (Exam exam : exams) {
                exam.setNumQuestions(exam.getQuestions().size());
                examRepository.save(exam);
            }
        };
    }
}
