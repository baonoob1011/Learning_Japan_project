package com.example.learningApp.mapper;


import com.example.learningApp.dto.request.course.CreateCourseRequest;
import com.example.learningApp.dto.request.exam.CreateSectionRequest;
import com.example.learningApp.dto.response.course.CourseResponse;
import com.example.learningApp.dto.response.exam.SectionResponse;
import com.example.learningApp.entity.Course;
import com.example.learningApp.entity.ExamSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toCourse(CreateCourseRequest createCourseRequest);
    @Mapping(target = "createdBy", expression = "java(getCreatedByName(course))")
    CourseResponse toCourseResponse(Course course);

    default String getCreatedByName(Course course) {
        if (course.getCreatedBy() == null) return null;
        return course.getCreatedBy().getFullName(); // 👈 hoặc getUsername()
    }}
