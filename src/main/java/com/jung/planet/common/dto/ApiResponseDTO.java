package com.jung.planet.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private boolean success;
    private T data;
    private String message;
    private Map<String, String> errors;

    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message("요청이 성공적으로 처리되었습니다.")
                .build();
    }

    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static ApiResponseDTO<Void> success(String message) {
        return ApiResponseDTO.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static ApiResponseDTO<Void> error(String message) {
        return ApiResponseDTO.<Void>builder()
                .success(false)
                .message(message)

                .build();
    }
    
    public static <T> ApiResponseDTO<T> error(String message, T errors) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .data(errors)
                .build();
    }
    
    public static ApiResponseDTO<Void> error(String message, Map<String, String> errors) {
        return ApiResponseDTO.<Void>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}