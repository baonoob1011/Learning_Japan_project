package com.example.learningApp.common;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    @Builder.Default
    boolean success = true;

    @Builder.Default
    int code = 200;

    String message;
    T result;



    public static <T> ApiResponse<T> of(boolean success, int httpStatus, String message, T result) {
        return ApiResponse.<T>builder()
                .success(success)
                .code(httpStatus)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success(T result) {
        return of(true, 200, "OK", result);
    }

    public static <T> ApiResponse<T> success(String message, T result) {
        return of(true, 200, message, result);
    }

    public static <T> ApiResponse<T> error(int httpStatus, String message) {
        return of(false, httpStatus, message, null);
    }

}
