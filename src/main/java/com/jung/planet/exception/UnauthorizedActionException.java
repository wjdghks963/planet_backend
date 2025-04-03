package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends BaseException {
    
    private static final String ERROR_CODE = "unauthorized.action";
    
    public UnauthorizedActionException(String message) {
        super(message);
    }
    
    public UnauthorizedActionException() {
        super(ErrorMessages.UNAUTHORIZED_ACTION);
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
