package com.example.learningApp.exception;

import com.example.learningApp.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Sai username / password (AWS Cognito)
    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotAuthorized(NotAuthorizedException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("Unauthorized: invalid username or password")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 🔹 Validation error
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .result(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 🔹 NullPointerException
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<String>> handleNullPointerException(NullPointerException ex) {
        log.error("❌ NullPointerException", ex);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Unexpected null value occurred")
                .result(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 🔹 Lỗi tạo Bean (config sai)
    @ExceptionHandler(BeanCreationException.class)
    public ResponseEntity<ApiResponse<String>> handleBeanCreationException(BeanCreationException ex) {
        log.error("⚙️ BeanCreationException", ex);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Application configuration error")
                .result(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 🔹 Bắt tất cả lỗi còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
        log.error("🔥 Unhandled Exception", ex);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .result(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
