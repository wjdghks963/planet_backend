package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 처리 중 발생하는 일반적인 예외
 */
public class ServiceException extends BaseException {
    
    private static final String ERROR_CODE = "service.error";
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    
    public ServiceException() {
        super(ErrorMessages.SERVER_ERROR);
    }
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    public ServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return this.status;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 