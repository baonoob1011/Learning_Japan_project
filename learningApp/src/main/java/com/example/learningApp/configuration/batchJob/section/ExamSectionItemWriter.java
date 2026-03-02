package com.example.learningApp.configuration.batchJob.section;

import com.example.learningApp.entity.ExamSection;
import com.example.learningApp.repository.ExamSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class ExamSectionItemWriter {

    private final ExamSectionRepository sectionRepository;

    @Bean(name = "examSectionWriter")
    public ItemWriter<ExamSection> examSectionWriter() {
        return items -> {
            List<ExamSection> sections = StreamSupport
                    .stream(items.spliterator(), false)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            sectionRepository.saveAll(sections);
        };
    }
}
