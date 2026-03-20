package com.example.learningApp.service.course.section.lesson;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.lesson.LessonDocumentResponse;
import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.LessonDocument;
import com.example.learningApp.repository.LessonDocumentRepository;
import com.example.learningApp.service.cloud.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonDocumentService {

    private final LessonDocumentRepository lessonDocumentRepository;
    private final EntityFinder finder;
    private final S3Service s3Service;

    /* ===================== CREATE ===================== */

    public String create(
            String lessonId,
            String title,
            Integer documentOrder,
            MultipartFile file
    ) throws IOException {

        Lesson lesson = finder.lessonId(lessonId);

        // ✅ Upload file lên S3
        String s3Url = s3Service.uploadLessonDocument(file, title);

        LessonDocument document = LessonDocument.builder()
                .lesson(lesson)
                .title(title)
                .documentUrl(s3Url)   // <-- gắn URL S3
                .documentOrder(documentOrder)
                .build();

        lessonDocumentRepository.save(document);

        return "Create lesson document successfully";
    }

    /* ===================== GET BY LESSON ===================== */

    public List<LessonDocumentResponse> getByLesson(String lessonId) {

        Lesson lesson = finder.lessonId(lessonId);

        return lessonDocumentRepository
                .findByLessonOrderByDocumentOrderAsc(lesson)
                .stream()
                .map(doc -> LessonDocumentResponse.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .documentUrl(doc.getDocumentUrl())
                        .documentOrder(doc.getDocumentOrder())
                        .build())
                .toList();
    }

    /* ===================== GET DETAIL ===================== */

    public LessonDocumentResponse getDetail(String documentId) {

        LessonDocument document = finder.lessonDocumentId(documentId);

        return LessonDocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .documentUrl(document.getDocumentUrl())
                .documentOrder(document.getDocumentOrder())
                .build();
    }

    /* ===================== DELETE ===================== */

    public void delete(String documentId) {

        LessonDocument document = finder.lessonDocumentId(documentId);
        lessonDocumentRepository.delete(document);
    }
}

