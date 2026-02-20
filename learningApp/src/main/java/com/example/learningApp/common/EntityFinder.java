package com.example.learningApp.common;


import com.example.learningApp.entity.*;
import com.example.learningApp.exception.NotFoundException;
import com.example.learningApp.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.example.learningApp.utils.RepositoryUtil.findOrThrow;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EntityFinder {

    UserRepository userRepository;
    YoutubeVideoRepository youtubeVideoRepository;
    VocabRepository vocabRepository;
    CourseRepository courseRepository;
    LessonRepository lessonRepository;
    SectionRepository sectionRepository;
    LessonPartRepository lessonPartRepository;
    LessonDocumentRepository lessonDocumentRepository;
    ChatRoomRepository chatRoomRepository;
    VipPackageRepository vipPackageRepository;

    public User userById() {

        return findOrThrow(
                userRepository,
                SecurityContextHolder.getContext().getAuthentication().getName(),
                () -> new NotFoundException("User not found")
        );
    }
    public Vocab vocabBySurface(String surface) {
        return vocabRepository.findBySurface(surface)
                .orElseThrow(() ->
                        new NotFoundException("Vocab not found: " + surface)
                );
    }

    public User userId(String userId) {
        return findOrThrow(
                userRepository,
                userId,
                () -> new NotFoundException("User not found")
        );
    }

    public ChatRoom chatRoomById(String chatRoomId) {
        return findOrThrow(
                chatRoomRepository,
                chatRoomId,
                () -> new NotFoundException("Room not found")
        );
    }

    public VipPackage vipPackageById(String vipPackageById) {
        return findOrThrow(
                vipPackageRepository,
                vipPackageById,
                () -> new NotFoundException("Vip Package not found")
        );
    }


    public YoutubeVideo videoById(String surface) {
        return findOrThrow(
                youtubeVideoRepository,
                surface,
                () -> new NotFoundException("Video not found")
        );
    }

    public LessonDocument lessonDocumentId(String lessonDocumentId) {
        return findOrThrow(
                lessonDocumentRepository,
                lessonDocumentId,
                () -> new NotFoundException("lesson Document not found")
        );
    }

    public LessonPart lessonPartId(String lessonPartId) {
        return findOrThrow(
                lessonPartRepository,
                lessonPartId,
                () -> new NotFoundException("lessonPart not found")
        );
    }

    public Course courseById(String courseId) {
        return findOrThrow(
                courseRepository,
                courseId,
                () -> new NotFoundException("Course not found")
        );
    }

    public Lesson lessonId(String lessonId) {
        return findOrThrow(
                lessonRepository,
                lessonId,
                () -> new NotFoundException("Lesson not found")
        );
    }

    public Section sectionId(String sectionId) {
        return findOrThrow(
                sectionRepository,
                sectionId,
                () -> new NotFoundException("Section not found")
        );
    }
    public Vocab vocabId(String vocabId) {
        return findOrThrow(
                vocabRepository,
                vocabId,
                () -> new NotFoundException("Vocab not found")
        );
    }
}
