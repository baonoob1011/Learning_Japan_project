package com.example.learningApp.service.lesson;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.lesson.CreateLessonPartRequest;
import com.example.learningApp.dto.response.lesson.LessonPartResponse;
import com.example.learningApp.entity.Lesson;
import com.example.learningApp.entity.LessonPart;
import com.example.learningApp.mapper.LessonPartMapper;
import com.example.learningApp.repository.LessonPartRepository;
import com.example.learningApp.repository.LessonRepository;
import com.example.learningApp.service.video.YoutubeVideoInfoService;
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

    public LessonPartResponse getDetail(String lessonPartId) {
        LessonPart lessonPart = finder.lessonPartId(lessonPartId);
        return lessonPartMapper.toLessonPartResponse(lessonPart);
    }

    public void deleteLessonPart(String lessonPartId) {
        LessonPart lessonPart = finder.lessonPartId(lessonPartId);
        lessonPartRepository.delete(lessonPart);
    }

}
