package com.example.learningApp.service.logging;

import com.example.learningApp.entity.SystemLog;

public interface LogService {
    void saveAsync(SystemLog systemLog);
}

