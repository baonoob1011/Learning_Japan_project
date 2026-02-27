package com.example.learningApp.repository;

import com.example.learningApp.entity.VipSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VipSubscriptionRepository extends JpaRepository<VipSubscription, String> {
    List<VipSubscription> findByExpiredDateBeforeAndActiveTrue(LocalDateTime now);

    boolean existsByUserIdAndActiveTrue(String userId);}
