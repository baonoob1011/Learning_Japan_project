package com.example.learningApp.service.feeback;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.request.feedback.AdminUpdateFeedbackRequest;
import com.example.learningApp.dto.request.feedback.CreateFeedbackRequest;
import com.example.learningApp.dto.response.feedback.FeedbackResponse;
import com.example.learningApp.entity.Feedback;
import com.example.learningApp.entity.User;
import com.example.learningApp.enums.FeedbackStatus;
import com.example.learningApp.mapper.FeedbackMapper;
import com.example.learningApp.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EntityFinder entityFinder;
    private final FeedbackMapper feedbackMapper;

    // ✅ Create Feedback
    public FeedbackResponse createFeedback( CreateFeedbackRequest request) {

        // Lấy user
        User user = entityFinder.userById();

        // MapStruct convert request → entity
        Feedback feedback = feedbackMapper.toEntity(request);

        // Set các field hệ thống
        feedback.setUser(user);
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);

        // MapStruct convert entity → response
        return feedbackMapper.toResponse(feedback);
    }
// ================= ADMIN UPDATE =================

    public FeedbackResponse updateFeedback(String feedbackId,
                                           AdminUpdateFeedbackRequest request) {

        Feedback feedback = entityFinder.findFeedbackById(feedbackId);

        // Cập nhật status nếu có
        if (request.getStatus() != null) {
            feedback.setStatus(request.getStatus());

            // Nếu RESOLVED thì set resolvedAt
            if (request.getStatus() == FeedbackStatus.RESOLVED) {
                feedback.setResolvedAt(LocalDateTime.now());
            }
        }

        // Cập nhật phản hồi admin
        if (request.getAdminReply() != null) {
            feedback.setAdminReply(request.getAdminReply());
        }

        feedbackRepository.save(feedback);

        return feedbackMapper.toResponse(feedback);
    }
    // ✅ Lấy feedback của user
    public List<FeedbackResponse> getMyFeedbacks() {
        User user = entityFinder.userById();
        return feedbackRepository.findByUserId(user.getId())
                .stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll()
                .stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }

    /**
     * Admin filter theo status
     */
    public List<FeedbackResponse> getFeedbacksByStatus(FeedbackStatus status) {
        return feedbackRepository.findByStatus(status)
                .stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }
}