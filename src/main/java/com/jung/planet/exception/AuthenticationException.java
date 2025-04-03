package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 인증 관련 예외
 * (JWT 토큰 관련 예외 등)
 */
public class AuthenticationException extends BaseException {
    
    private static final String ERROR_CODE = "authentication.failed";
    
    public AuthenticationException() {
        super(ErrorMessages.AUTHENTICATION_FAILED);
    }
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 