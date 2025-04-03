package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 접근 권한이 없는 리소스에 접근 시도할 때 발생하는 예외
 * (AccessDeniedException, UnauthorizedActionException을 대체)
 */
public class PermissionDeniedException extends BaseException {
    
    private static final String DEFAULT_MESSAGE = "해당 리소스에 대한 접근 권한이 없습니다.";
    private static final String ERROR_CODE = "permission.denied";
    
    public PermissionDeniedException() {
        super(DEFAULT_MESSAGE);
    }
    
    public PermissionDeniedException(String message) {
        super(message);
    }
    
    public PermissionDeniedException(String resourceType, Long resourceId) {
        super(String.format("%s(ID: %d)에 대한 접근 권한이 없습니다.", resourceType, resourceId));
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
} 