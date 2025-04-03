package com.jung.planet.common.dto;

import com.jung.planet.exception.BaseException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "API 응답 모델")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    @Schema(description = "요청 성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "에러 코드 (오류 발생 시)", example = "resource.not.found")
    private String errorCode;
    
    @Schema(description = "필드별 상세 오류 정보 (검증 오류 발생 시)")
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
    
    public static ApiResponseDTO<Void> error(String message, String errorCode) {
        return ApiResponseDTO.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    public static ApiResponseDTO<Void> error(BaseException exception) {
        return ApiResponseDTO.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .errorCode(exception.getErrorCode())
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
    
    public static ApiResponseDTO<Void> error(String message, String errorCode, Map<String, String> errors) {
        return ApiResponseDTO.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .build();
    }
}