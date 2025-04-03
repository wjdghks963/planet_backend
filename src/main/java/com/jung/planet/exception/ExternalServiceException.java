package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 외부 서비스(AWS S3, Cloudflare 등)와의 통신 중 발생하는 예외
 */
public class ExternalServiceException extends BaseException {
    
    private static final String ERROR_CODE = "external.service.error";
    
    public ExternalServiceException() {
        super(ErrorMessages.EXTERNAL_SERVICE_ERROR);
    }
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExternalServiceException(String serviceName, String operation, Throwable cause) {
        super(String.format("%s 서비스의 %s 작업 중 오류가 발생했습니다: %s", serviceName, operation, cause.getMessage()), cause);
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 