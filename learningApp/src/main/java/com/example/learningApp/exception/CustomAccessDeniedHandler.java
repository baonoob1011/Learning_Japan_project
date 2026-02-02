
package com.example.learningApp.exception;

import com.example.learningApp.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class CustomAccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            log.warn("Access denied: {}", ex.getMessage());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpServletResponse.SC_FORBIDDEN)
                    .message("Access Denied: You do not have permission to access this resource.")
                    .result(ex.getMessage())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        };
    }
}

