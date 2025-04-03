package com.jung.planet.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 커스텀 예외의 기본 클래스
 * 모든 비즈니스 예외는 이 클래스를 상속받아야 함
 */
public abstract class BaseException extends RuntimeException {
    
    /**
     * 이 예외와 연관된 HTTP 상태 코드
     * @return HTTP 상태 코드
     */
    public abstract HttpStatus getHttpStatus();
    
    /**
     * 예외 코드 (국제화 메시지 키 또는 클라이언트 에러 코드로 사용)
     * @return 예외 코드
     */
    public abstract String getErrorCode();
    
    public BaseException(String message) {
        super(message);
    }
    
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
} 