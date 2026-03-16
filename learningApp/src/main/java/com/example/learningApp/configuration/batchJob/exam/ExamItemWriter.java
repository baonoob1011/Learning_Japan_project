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

            Set<Exam> exams = list.stream()
                    .map(Question::getSection)
                    .flatMap(s -> s.getExams().stream())
                    .collect(Collectors.toSet());

            for (Exam exam : exams) {
                // ⚡ Liên kết trực tiếp Question <-> Exam
                List<Question> questionsForThisExam = list.stream()
                        .filter(q -> q.getSection().getExams().contains(exam))
                        .collect(Collectors.toList());

                for (Question q : questionsForThisExam) {
                    if (!q.getExams().contains(exam)) {
                        q.getExams().add(exam);
                    }
                    if (!exam.getQuestions().contains(q)) {
                        exam.getQuestions().add(q);
                    }
                }

                long totalQuestions = questionRepository.countAllByExamId(exam.getId());
                exam.setNumQuestions((int) totalQuestions);
                examRepository.save(exam);
            }

            // Re-save questions to persist the relationship from the Question side if
            // needed
            // (though joining through Exam.save should handle it)
            questionRepository.saveAll(list);
        };
    }
}
