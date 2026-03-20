package com.example.learningApp.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://nibojapan.cloud",
            "https://www.nibojapan.cloud",
            "https://learning-japan-project-fe.vercel.app"
    );

    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of(
            "https://*.vercel.app"
    );

    @PostConstruct
    public void logCorsConfigurationOnStartup() {
        log.info("CORS deployed config is active. allowedOrigins={}", ALLOWED_ORIGINS);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> corsDebugLoggingFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String origin = request.getHeader("Origin");
                boolean hasOrigin = origin != null && !origin.isBlank();

                if (hasOrigin) {
                    log.info("CORS request: method={}, path={}, origin={}",
                            request.getMethod(), request.getRequestURI(), origin);
                }

                filterChain.doFilter(request, response);

                if (hasOrigin) {
                    log.info("CORS response: method={}, path={}, origin={}, allowOrigin={}, allowCredentials={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            origin,
                            response.getHeader("Access-Control-Allow-Origin"),
                            response.getHeader("Access-Control-Allow-Credentials"));
                }
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        registration.addUrlPatterns("/api/*");
        return registration;
    }

}
