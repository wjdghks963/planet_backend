package com.jung.planet.exception;

import com.jung.planet.admin.service.SlackNotificationService;
import com.jung.planet.common.dto.ApiResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final SlackNotificationService slackNotificationService;

    /**
     * 새로운 베이스 예외 처리 핸들러
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBaseException(BaseException e, HttpServletRequest request) {
        HttpStatus status = e.getHttpStatus();
        
        // 서버 에러인 경우에만 슬랙 알림
        if (status.is5xxServerError()) {
            slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
            log.error("Server error occurred", e);
        } else {
            log.warn("Client error occurred: {}", e.getMessage());
        }
        
        return ResponseEntity
                .status(status)
                .body(ApiResponseDTO.error(e));
    }
    
    /**
     * EntityNotFoundException 처리 - ResourceNotFoundException으로 마이그레이션을 위한 임시 핸들러
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDTO<Void> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ApiResponseDTO.error(e.getMessage());
    }

    /**
     * 기존 AccessDeniedException 처리 - PermissionDeniedException으로 마이그레이션을 위한 임시 핸들러
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(e.getMessage());
    }

    /**
     * 기존 UnauthorizedActionException 처리 - PermissionDeniedException으로 마이그레이션을 위한 임시 핸들러
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseDTO<Void> handleUnauthorizedActionException(UnauthorizedActionException e) {
        log.warn("Unauthorized action: {}", e.getMessage());
        return ApiResponseDTO.error(e.getMessage());
    }

    /**
     * ExpiredJwtException 처리
     * AuthenticationException으로 점진적으로 마이그레이션
     */
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponseDTO<Void> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("JWT token expired: {}", ex.getMessage());
        return ApiResponseDTO.error(ErrorMessages.JWT_EXPIRED);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Void> handleDatabaseException(DataAccessException e, HttpServletRequest request) {
        log.error("Database error", e);
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(ErrorMessages.DATABASE_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        Map<String, String> errors = fieldErrors.stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        log.warn("Validation error: {}", errors);
        
        return ApiResponseDTO.<Map<String, String>>builder()
                .success(false)
                .message(ErrorMessages.VALIDATION_FAILED)
                .data(errors)
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDTO<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ApiResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(SdkClientException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Void> handleSdkClientException(SdkClientException e, HttpServletRequest request) {
        log.error("AWS SDK client error", e);
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(ErrorMessages.EXTERNAL_SERVICE_ERROR + ": " + e.getMessage());
    }
    
    /**
     * 처리되지 않은 모든 예외에 대한 핸들러
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDTO<Void> handleAllException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception occurred", e);
        slackNotificationService.sendSlackErrorNotification(e.getMessage(), request);
        return ApiResponseDTO.error(ErrorMessages.SERVER_ERROR);
    }
}
