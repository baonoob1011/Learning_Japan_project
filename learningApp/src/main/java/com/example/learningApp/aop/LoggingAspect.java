package com.example.learningApp.aop;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.SystemLog;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.logging.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingAspect {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "token", "accesstoken", "refreshtoken", "secret", "authorization"
    );

    private final LogService logService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    /**
     * Only keep important entry-point flows at controller level.
     * We intentionally skip service-level logs to avoid duplicate/noisy logs.
     */
    private static final Set<String> MAINFLOW_METHOD_PREFIXES = Set.of(
            "login", "logout", "refresh", "register",
            "create", "save", "upload", "submit", "start",
            "mark", "finalize", "attempt", "grade",
            "purchase", "confirm", "forgot", "run",
            "check", "translate", "generate"
    );

    private static final Set<String> MAINFLOW_METHOD_NAMES = Set.of(
            "getTodaySession",
            "getTodayReviews",
            "getMyVocabProgress",
            "getOverallProgress",
            "finalizeSmartWord",
            "attemptSmartSkill"
    );

    @Pointcut("execution(* com.example.learningApp.controller..*(..))")
    public void controllerPackage() {
    }

    @Pointcut("execution(* com.example.learningApp.service..*(..))")
    public void servicePackage() {
    }

    @Pointcut("@annotation(com.example.learningApp.annotation.NoLog) || @within(com.example.learningApp.annotation.NoLog)")
    public void noLogPointcut() {
    }

    @Around("(controllerPackage() || servicePackage()) && !noLogPointcut() && !execution(* com.example.learningApp.service.logging..*(..))")
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.currentTimeMillis();
        String username = extractUsername();
        String ipAddress = extractIpAddress();
        String targetClass = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        if (!shouldLogMainFlow(targetClass, methodName)) {
            return joinPoint.proceed();
        }

        String argsJson = serializeArguments(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            long executionMs = System.currentTimeMillis() - startedAt;

            SystemLog systemLog = new SystemLog();
            systemLog.setUsername(username);
            systemLog.setIpAddress(ipAddress);
            systemLog.setTargetClass(targetClass);
            systemLog.setMethodName(methodName);
            systemLog.setArguments(argsJson);
            systemLog.setResult(serializeValue(result));
            systemLog.setExecutionTime(executionMs);
            systemLog.setStatus("SUCCESS");
            systemLog.setCreatedAt(LocalDateTime.now());

            logService.saveAsync(systemLog);
            return result;
        } catch (Throwable throwable) {
            long executionMs = System.currentTimeMillis() - startedAt;

            SystemLog systemLog = new SystemLog();
            systemLog.setUsername(username);
            systemLog.setIpAddress(ipAddress);
            systemLog.setTargetClass(targetClass);
            systemLog.setMethodName(methodName);
            systemLog.setArguments(argsJson);
            systemLog.setExecutionTime(executionMs);
            systemLog.setStatus("FAILURE");
            systemLog.setErrorMessage(trimError(throwable));
            systemLog.setCreatedAt(LocalDateTime.now());

            logService.saveAsync(systemLog);
            throw throwable;
        }
    }

    private String extractUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "Người dùng ẩn danh";
            }
            String name = auth.getName();
            if (name == null || name.isBlank()) {
                return "Người dùng ẩn danh";
            }
            return resolveDisplayUsername(name);
        } catch (Exception ex) {
            return "Người dùng ẩn danh";
        }
    }

    private String resolveDisplayUsername(String principalName) {
        if ("anonymousUser".equalsIgnoreCase(principalName)) {
            return "Người dùng ẩn danh";
        }

        if (principalName.contains("@")) {
            return principalName;
        }

        if (!looksLikeUuid(principalName)) {
            return principalName;
        }

        return userRepository.findById(principalName)
                .map(this::toDisplayName)
                .orElse(principalName);
    }

    private String toDisplayName(User user) {
        if (user == null) {
            return "Người dùng không xác định";
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return user.getId();
    }

    private boolean looksLikeUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String extractIpAddress() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (!(requestAttributes instanceof ServletRequestAttributes servletAttrs)) {
                return "N/A";
            }

            HttpServletRequest request = servletAttrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private String serializeArguments(Object[] args) {
        try {
            List<Object> sanitized = new ArrayList<>();
            if (args != null) {
                for (Object arg : args) {
                    sanitized.add(sanitizeForLogging(arg));
                }
            }
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception ex) {
            return "[\"<failed-to-serialize-arguments>\"]";
        }
    }

    private String serializeValue(Object value) {
        try {
            Object sanitized = sanitizeForLogging(value);
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException ex) {
            return "\"<failed-to-serialize-result>\"";
        }
    }

    private Object sanitizeForLogging(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof HttpServletRequest request) {
            return Map.of(
                    "type", "HttpServletRequest",
                    "method", request.getMethod(),
                    "uri", request.getRequestURI()
            );
        }
        if (value instanceof HttpServletResponse) {
            return Map.of("type", "HttpServletResponse");
        }
        if (value instanceof MultipartFile file) {
            Map<String, Object> fileInfo = new LinkedHashMap<>();
            fileInfo.put("type", "MultipartFile");
            fileInfo.put("originalFilename", file.getOriginalFilename());
            fileInfo.put("size", file.getSize());
            fileInfo.put("contentType", file.getContentType());
            return fileInfo;
        }
        if (value instanceof BindingResult) {
            return Map.of("type", "BindingResult");
        }
        if (value instanceof byte[]) {
            return Map.of("type", "byte[]", "length", ((byte[]) value).length);
        }
        if (value instanceof InputStream || value instanceof Reader) {
            return Map.of("type", value.getClass().getSimpleName());
        }

        JsonNode node = objectMapper.valueToTree(value);
        maskSensitive(node);
        return node;
    }

    private void maskSensitive(JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey() == null ? "" : field.getKey().toLowerCase(Locale.ROOT);
                if (SENSITIVE_KEYS.contains(key)) {
                    objectNode.put(field.getKey(), "******");
                } else {
                    maskSensitive(field.getValue());
                }
            }
            return;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                maskSensitive(child);
            }
        }
    }

    private String trimError(Throwable throwable) {
        String message = throwable.getClass().getSimpleName() + ": " + String.valueOf(throwable.getMessage());
        if (message.length() <= 4000) {
            return message;
        }
        return message.substring(0, 4000);
    }

    private boolean shouldLogMainFlow(String targetClass, String methodName) {
        if (targetClass == null || methodName == null) {
            return false;
        }

        // Log only controller entry-points (skip service-level duplicate logs).
        if (!targetClass.contains(".controller.")) {
            return false;
        }

        // Never log the log-view endpoint itself (self-referential noise).
        if ("com.example.learningApp.controller.logging.AdminSystemLogController".equals(targetClass)) {
            return false;
        }

        // Skip high-frequency polling notification endpoints.
        if ("com.example.learningApp.controller.notification.NotificationController".equals(targetClass)
                && ("getUnreadCount".equals(methodName) || "getMyNotifications".equals(methodName))) {
            return false;
        }

        String lowerMethod = methodName.toLowerCase(Locale.ROOT);
        for (String prefix : MAINFLOW_METHOD_PREFIXES) {
            if (lowerMethod.startsWith(prefix)) {
                return true;
            }
        }
        return MAINFLOW_METHOD_NAMES.contains(methodName);
    }
}
