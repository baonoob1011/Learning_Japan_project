package com.example.learningApp.configuration.security;

import com.example.learningApp.service.auth.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SingleSessionFilter extends OncePerRequestFilter {
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip non-API requests or specific Auth endpoints (Login, Init Session)
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.contains("/auth/login") || path.contains("/auth/session/init") 
            || path.contains("/users/online") || path.contains("/call-history")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get authentication from context (set by previous Cognito filter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String userId = auth.getName(); // Cognito 'sub'
            String sessionId = request.getHeader("X-Session-ID");

            // Kiểm tra xem user này đã "mở" quản lý Single Session trong Redis chưa?
            if (sessionService.hasActiveSession(userId)) {
                // Nếu ĐÃ có session trong Redis, thì request GỬI LÊN phải có sessionId và phải
                // KHỚP
                if (sessionId == null || !sessionService.isSessionValid(userId, sessionId)) {
                    log.warn("[AUTH_SESSION_REJECT] User {} session is invalid (Header SID={}). Reason: Active session found in Redis of this user.", userId, sessionId);
                    
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("{\"code\": 401, \"message\": \"Session invalidated by another login.\"}");
                    return;
                }
            } else {
                // Nếu CHƯA có session trong Redis (User cũ chưa login lại), tạm thời cho qua
                // log.debug("[AUTH_SESSION_SKIP] User {} has no active session in Redis yet.",
                // userId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
