package com.example.learningApp.configuration.batchJob;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class BatchUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static int parseIntSafe(String value, int defaultValue) {
        try {
            return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float parseFloatSafe(String value, float defaultValue) {
        try {
            if (value == null || value.isBlank()) return defaultValue;
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String parseOptions(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("[]")) return "[]";

        raw = raw.trim();
        if (raw.startsWith("[") && raw.endsWith("]")) {
            raw = raw.substring(1, raw.length() - 1);
        }

        List<String> list = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceAll("^\"|\"$", ""))
                .map(s -> s.replaceAll("^'|'$", ""))
                .toList();

        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
