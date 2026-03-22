package com.example.learningApp.service.logging;

import com.example.learningApp.entity.SystemLog;
import com.example.learningApp.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogServiceImpl implements LogService {

    private final SystemLogRepository systemLogRepository;

    @Override
    @Async("taskExecutor")
    public void saveAsync(SystemLog systemLog) {
        try {
            systemLogRepository.save(systemLog);
        } catch (Exception ex) {
            log.error("Failed to persist system log for {}.{}",
                    systemLog.getTargetClass(), systemLog.getMethodName(), ex);
        }
    }
}

