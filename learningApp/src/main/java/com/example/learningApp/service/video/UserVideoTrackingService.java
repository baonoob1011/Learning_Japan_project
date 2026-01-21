package com.example.learningApp.service.video;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.video.VideoProgressRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVideoTrackingService {

    VideoProgressMapper videoProgressMapper;
    UserVideoTrackingRepository videoTrackingRepository;
    EntityFinder finder;

    private static final long COMPLETE_EPSILON = 3; // lệch 3s

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


        // 3️⃣ update progress
        Long lastPos = request.getLastPositionSeconds();
        Long delta = request.getWatchedSecondsDelta();

        if (lastPos != null) {
            tracking.setLastPositionSeconds(lastPos);
        }

        if (delta != null && delta > 0) {
            long currentTotal =
                    tracking.getTotalWatchedSeconds() != null
                            ? tracking.getTotalWatchedSeconds()
                            : 0L;

            tracking.setTotalWatchedSeconds(currentTotal + delta);
        }

        tracking.setLastWatchedAt(Instant.now());

        // 4️⃣ auto-complete (parse duration)
        long durationSeconds =
                DurationUtils.parseToSeconds(video.getDuration());

        if (!tracking.isCompleted()
                && durationSeconds > 0
                && lastPos != null
                && lastPos >= durationSeconds - COMPLETE_EPSILON) {
            tracking.setCompleted(true);
        }

        // 5️⃣ save
        videoTrackingRepository.save(tracking);
    }
}
