package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 * (EntityNotFoundException을 대체)
 */
public class ResourceNotFoundException extends BaseException {
    
    private static final String ERROR_CODE = "resource.not.found";
    
    public ResourceNotFoundException() {
        super(ErrorMessages.RESOURCE_NOT_FOUND);
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 