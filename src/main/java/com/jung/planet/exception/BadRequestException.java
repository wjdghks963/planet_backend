package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 잘못된 요청을 보냈을 때 발생하는 예외
 * (IllegalArgumentException을 대체)
 */
public class BadRequestException extends BaseException {
    
    private static final String ERROR_CODE = "bad.request";
    
    public BadRequestException() {
        super(ErrorMessages.BAD_REQUEST);
    }
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 