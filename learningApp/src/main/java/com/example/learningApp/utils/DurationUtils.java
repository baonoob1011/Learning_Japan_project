package com.example.learningApp.utils;

import java.time.Duration;

public final class DurationUtils {

    private DurationUtils() {}

    // PT2H35M54S -> seconds
    public static long parseToSeconds(String isoDuration) {
        if (isoDuration == null || isoDuration.isBlank()) {
            return 0;
        }
        return Duration.parse(isoDuration).getSeconds();
    }
}

