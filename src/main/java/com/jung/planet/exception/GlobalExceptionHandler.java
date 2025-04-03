package com.jung.planet.exception;

import com.jung.planet.admin.service.SlackNotificationService;
import com.jung.planet.common.dto.ApiResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SlackNotificationService slackNotificationService;

    // EntityNotFoundException에 대한 처리
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDTO<Void> handleEntityNotFoundException(EntityNotFoundException e) {
        return ApiResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Void> handleDatabaseException(DataAccessException e, HttpServletRequest request) {
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error("서버 에러가 발생했습니다.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO<Void> handleExpiredJwtException(ExpiredJwtException ex) {
        return ApiResponseDTO.error("Refresh JWT Token이 만료되었습니다.");
    }

    // MethodArgumentNotValidException에 대한 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        Map<String, String> errors = fieldErrors.stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        // 타입 일치를 위해 Map<String, String>을 직접 data로 전달
        return ApiResponseDTO.<Map<String, String>>builder()
                .success(false)
                .message("요청 데이터 검증에 실패했습니다.")
                .data(errors)
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO<Void> handleUnauthorizedActionException(UnauthorizedActionException e) {
        return ApiResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(e.getMessage());
    }

    // SdkClientException에 대한 처리
    @ExceptionHandler(SdkClientException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Void> handleSdkClientException(SdkClientException e, HttpServletRequest request) {
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error("AWS S3 서비스 오류: " + e.getMessage());
    }
}
