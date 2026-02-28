package com.example.learningApp.service.video.comment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.video.comment.CreateCommentRequest;
import com.example.learningApp.dto.response.video.comment.VideoCommentResponse;
import com.example.learningApp.entity.*;
import com.example.learningApp.mapper.VideoCommentMapper;
import com.example.learningApp.repository.VideoCommentRepository;
import com.example.learningApp.repository.VideoRatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoCommentService {

    private final VideoCommentRepository commentRepository;
    private final VideoRatingRepository videoRatingRepository;
    private final EntityFinder entityFinder;
    private final VideoCommentMapper mapper;

    /* ================= CREATE COMMENT ================= */
    @Transactional
    public VideoCommentResponse createComment(CreateCommentRequest request) {

        User user = entityFinder.userById();
        YoutubeVideo video = entityFinder.videoById(request.getVideoId());

        // ================= COMMENT =================
        VideoComment parent = null;
        if (request.getParentId() != null) {
            parent = entityFinder.commentById(request.getParentId());
        }

        VideoComment comment = VideoComment.builder()
                .content(request.getContent())
                .user(user)
                .video(video)
                .parent(parent)
                .createdAt(Instant.now())
                .build();

        commentRepository.save(comment);

        Integer ratingValue = null;

        // ================= RATING (OPTIONAL) =================
        if (request.getRating() != null) {

            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }

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

            ratingValue = rating.getRating();
        }

        // ================= BUILD RATING MAP =================
        Map<String, Integer> ratingMap = ratingValue != null
                ? Map.of(user.getId(), ratingValue)
                : null;

        return mapper.toResponse(comment, ratingMap);
    }
    /* ================= GET COMMENTS ================= */

    public List<VideoCommentResponse> getComments(String videoId) {

        YoutubeVideo video = entityFinder.videoById(videoId);

        /* ===== LẤY RATING 1 LẦN ===== */
        List<VideoRating> ratings =
                videoRatingRepository.findAllByVideo(video);

        // tránh duplicate key crash
        Map<String, Integer> ratingMap = ratings.stream()
                .collect(Collectors.toMap(
                        r -> r.getUser().getId(),
                        VideoRating::getRating,
                        (existing, replacement) -> replacement
                ));

        /* ===== LẤY COMMENT ===== */
        List<VideoComment> comments =
                commentRepository
                        .findByVideoAndParentIsNullOrderByCreatedAtDesc(video);

        return comments.stream()
                .map(comment -> {

                    VideoCommentResponse response =
                            mapper.toResponse(comment, ratingMap);

                    // map replies
                    if (comment.getReplies() != null &&
                            !comment.getReplies().isEmpty()) {

                        response.setReplies(
                                mapper.toResponseList(
                                        comment.getReplies(),
                                        ratingMap
                                )
                        );
                    }

                    return response;
                })
                .toList();
    }
}