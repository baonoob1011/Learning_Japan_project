package com.example.learningApp.configuration.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest req) {
            String method = req.getMethod();
            String uri = req.getRequestURI();
            
            // Only log DELETE and notification-related requests for debugging
            if ("DELETE".equalsIgnoreCase(method) || uri.contains("notifications")) {
                log.info("[REQUEST_LOG_V1] Method: {}, URI: {}, Query: {}", 
                    method, uri, req.getQueryString());
            }
        }
        
        chain.doFilter(request, response);
    }
}
