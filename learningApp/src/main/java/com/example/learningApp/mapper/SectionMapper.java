package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.course.section.CreateSectionRequest;
import com.example.learningApp.dto.response.course.section.SectionResponse;
import com.example.learningApp.entity.Section;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface SectionMapper {
Section toSection(CreateSectionRequest createSectionRequest);
SectionResponse toSectionResponse(Section section);
}

