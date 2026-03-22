package com.example.learningApp.controller.logging;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.common.PageResponse;
import com.example.learningApp.entity.SystemLog;
import com.example.learningApp.repository.SystemLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping({"/api/v1/admin/system-logs", "/api/v1/admin/system-logs."})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemLogController {

    SystemLogRepository systemLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SystemLog>>> getSystemLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = Math.max(size, 1);
        LocalDateTime effectiveFrom = fromTime != null ? fromTime : from;
        LocalDateTime effectiveTo = toTime != null ? toTime : to;

        Page<SystemLog> logs = systemLogRepository.searchLogs(
                normalize(keyword),
                normalize(username),
                normalize(status),
                effectiveFrom,
                effectiveTo,
                PageRequest.of(safePage, safeSize)
        );

        PageResponse<SystemLog> response = PageResponse.<SystemLog>builder()
                .data(logs.getContent())
                .page(logs.getNumber() + 1)
                .size(logs.getSize())
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success("System logs retrieved successfully", response));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @DeleteMapping("/all")
    @Transactional
    public ResponseEntity<ApiResponse<Integer>> deleteAllSystemLogs() {
        int deleted = systemLogRepository.deleteAllLogs();
        return ResponseEntity.ok(ApiResponse.success("Deleted all system logs", deleted));
    }
}
