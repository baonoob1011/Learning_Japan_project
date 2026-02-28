package com.example.learningApp.service.video.rating;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.video.rating.RateVideoRequest;
import com.example.learningApp.dto.response.video.rating.VideoRatingResponse;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VideoRating;
import com.example.learningApp.entity.YoutubeVideo;
import com.example.learningApp.repository.VideoRatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoRatingService {

    private final VideoRatingRepository videoRatingRepository;
    private final EntityFinder entityFinder;

    /**
     * Rate hoặc update rating
     */
    public void rateVideo(RateVideoRequest request) {

        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = entityFinder.userById();
        YoutubeVideo video = entityFinder.videoById(request.getVideoId());

        VideoRating rating = videoRatingRepository
                .findByUserAndVideo(user, video)
                .orElse(
                        VideoRating.builder()
                                .user(user)
                                .video(video)
                                .createdAt(Instant.now())
                                .build()
                );

        rating.setRating(request.getRating());

        videoRatingRepository.save(rating);
    }

    /**
     * Lấy rating trung bình + tổng số lượt đánh giá
     */
    public VideoRatingResponse getVideoRating(String videoId) {

        YoutubeVideo video = entityFinder.videoById(videoId);

        Double avg = videoRatingRepository.getAverageRating(video);
        Long total = videoRatingRepository.countByVideo(video);

        return VideoRatingResponse.builder()
                .averageRating(avg == null ? 0 : avg)
                .totalRatings(total == null ? 0 : total.intValue())
                .build();
    }

    /**
     * Xóa rating của user hiện tại
     */
    public void deleteMyRating(String videoId) {

        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = entityFinder.userById();
        YoutubeVideo video = entityFinder.videoById(videoId);

        videoRatingRepository
                .findByUserAndVideo(user, video)
                .ifPresent(videoRatingRepository::delete);
    }
}
