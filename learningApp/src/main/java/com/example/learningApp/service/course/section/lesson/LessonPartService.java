package com.example.learningApp.service.course.section.lesson;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.course.section.lesson.CreateLessonPartRequest;
import com.example.learningApp.dto.request.course.section.lesson.UpdateLessonPartRequest;
import com.example.learningApp.dto.response.lesson.LessonPartResponse;
import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.LessonPart;
import com.example.learningApp.mapper.LessonPartMapper;
import com.example.learningApp.repository.LessonPartRepository;
import com.example.learningApp.service.video.YoutubeVideoInfoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonPartService {

    private final LessonPartRepository lessonPartRepository;
    private final EntityFinder finder;
    private final LessonPartMapper lessonPartMapper;
    private final YoutubeVideoInfoService youtubeVideoInfoService;

    public String createLessonPart(CreateLessonPartRequest request)
            throws IOException, InterruptedException {

        Lesson lesson = finder.lessonId(request.getLessonId());

        LessonPart lessonPart = lessonPartMapper.toLessonPart(request);
        lessonPart.setLesson(lesson);

        //  Nếu có videoUrl thì lấy duration
        if (request.getVideoUrl() != null && !request.getVideoUrl().isBlank()) {

            String videoId = youtubeVideoInfoService.extractVideoId(request.getVideoUrl());

            var youtubeVideo = youtubeVideoInfoService
                    .fetchAndSaveVideoInfo(request.getVideoUrl(), videoId);

            lessonPart.setDuration(youtubeVideo.getDuration());
        }

        lessonPartRepository.save(lessonPart);

        return "Create lesson part successfully";
    }
    public List<LessonPartResponse> getByLesson(String lessonId) {
        Lesson lesson = finder.lessonId(lessonId);
        return lessonPartRepository.findByLesson(lesson)
                .stream()
                .map(lessonPartMapper::toLessonPartResponse)
                .toList();
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public String updateLessonPart(String lessonPartId,
                                   UpdateLessonPartRequest request)
            throws IOException, InterruptedException {

        LessonPart lessonPart = finder.lessonPartId(lessonPartId);

        if (request.getTitle() != null) {
            lessonPart.setTitle(request.getTitle());
        }

        if (request.getLessonPartType() != null) {
            lessonPart.setLessonPartType(request.getLessonPartType());
        }

        if (request.getPartOrder() != null) {
            lessonPart.setPartOrder(request.getPartOrder());
        }

        // Nếu đổi videoUrl
        if (request.getVideoUrl() != null &&
                !request.getVideoUrl().isBlank() &&
                !request.getVideoUrl().equals(lessonPart.getVideoUrl())) {

            lessonPart.setVideoUrl(request.getVideoUrl());

            String videoId =
                    youtubeVideoInfoService.extractVideoId(request.getVideoUrl());

            var youtubeVideo =
                    youtubeVideoInfoService.fetchAndSaveVideoInfo(
                            request.getVideoUrl(), videoId
                    );

            lessonPart.setDuration(youtubeVideo.getDuration());
        }

        return "Update lesson part successfully";
    }
    public LessonPartResponse getDetail(String lessonPartId) {
        LessonPart lessonPart = finder.lessonPartId(lessonPartId);
        return lessonPartMapper.toLessonPartResponse(lessonPart);
    }

    public void deleteLessonPart(String lessonPartId) {
        LessonPart lessonPart = finder.lessonPartId(lessonPartId);
        lessonPartRepository.delete(lessonPart);
    }

}
