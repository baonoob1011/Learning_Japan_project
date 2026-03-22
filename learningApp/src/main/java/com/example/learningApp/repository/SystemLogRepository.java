package com.example.learningApp.repository;

import com.example.learningApp.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLog, String> {
}

