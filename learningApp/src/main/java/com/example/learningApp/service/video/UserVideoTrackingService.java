package com.example.learningApp.service.video;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.video.VideoProgressRequest;
import com.example.learningApp.dto.response.video.VideoProgressResponse;
import com.example.learningApp.entity.UserVideoTracking;
import com.example.learningApp.mapper.VideoProgressMapper;
import com.example.learningApp.repository.UserVideoTrackingRepository;
import com.example.learningApp.utils.DurationUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVideoTrackingService {

    VideoProgressMapper videoProgressMapper;
    UserVideoTrackingRepository videoTrackingRepository;
    EntityFinder finder;
    private static final long MAX_DELTA_SECONDS = 6;
    private static final long SEEK_EPSILON = 5;       // cho tua hơn 5s

    public void saveUserVideoTracking(VideoProgressRequest request) {

        var user = finder.userById();
        var video = finder.videoById(request.getVideoId());

        var tracking = videoTrackingRepository
                .findByUserAndVideo(user, video)
                .orElseGet(() -> UserVideoTracking.builder()
                        .user(user)
                        .video(video)
                        .completed(false)
                        .totalWatchedSeconds(0L)
                        .lastPositionSeconds(0L)
                        .build());

        Long lastPos = request.getLastPositionSeconds();
        Long delta = request.getWatchedSecondsDelta();

        long currentTotal =
                tracking.getTotalWatchedSeconds() != null
                        ? tracking.getTotalWatchedSeconds()
                        : 0L;

        // ===============================
        // 1️⃣ CHẶN TUA QUÁ XA
        // ===============================
        if (lastPos != null) {
            long maxAllowedSeek = currentTotal + SEEK_EPSILON; // cho phép hơn chút cho UX

            if (lastPos > maxAllowedSeek) {
                // ❌ tua gian lận → khóa lại
                lastPos = maxAllowedSeek;
            }

            // ❌ KHÔNG cho lùi lại dưới progress đã xem
            if (lastPos < tracking.getLastPositionSeconds()) {
                lastPos = tracking.getLastPositionSeconds();
            }

            tracking.setLastPositionSeconds(lastPos);
        }

        // ===============================
        // 2️⃣ CHỈ CỘNG WATCHED KHI XEM THẬT
        // ===============================
        if (delta != null && delta > 0) {
            // ❌ chống spam / fake delta
            if (delta <= MAX_DELTA_SECONDS) {
                tracking.setTotalWatchedSeconds(currentTotal + delta);
            }
        }

        tracking.setLastWatchedAt(Instant.now());

        // ===============================
        // 3️⃣ COMPLETE = DỰA VÀO TOTAL WATCHED
        // ===============================
        long durationSeconds = DurationUtils.parseToSeconds(video.getDuration());

        if (!tracking.isCompleted()
                && durationSeconds > 0
                && tracking.getTotalWatchedSeconds() >= durationSeconds * 0.9) {
            tracking.setCompleted(true);
        }

        // ===============================
        // 4️⃣ SAVE
        // ===============================
        videoTrackingRepository.save(tracking);
    }

    public List<VideoProgressResponse> getAllUserVideoProgress() {

        var user = finder.userById();

        return videoTrackingRepository
                .findAllByUser(user)
                .stream()
                .map(videoProgressMapper::toVideoProgressResponse)
                .toList();
    }


}

