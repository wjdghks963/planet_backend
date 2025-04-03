package com.jung.planet.exception;

/**
 * 애플리케이션 전체에서 사용되는 에러 메시지를 상수로 정의합니다.
 * Swagger 문서화와 예외 메시지의 일관성을 유지하기 위해 사용됩니다.
 */
public final class ErrorMessages {
    // 리소스 관련 에러 메시지
    public static final String RESOURCE_NOT_FOUND = "요청한 리소스를 찾을 수 없습니다.";
    public static final String RESOURCE_ALREADY_EXISTS = "이미 존재하는 리소스입니다.";
    public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다.";
    public static final String PLANT_NOT_FOUND = "식물을 찾을 수 없습니다.";
    public static final String DIARY_NOT_FOUND = "다이어리를 찾을 수 없습니다.";
    
    // 권한 관련 에러 메시지
    public static final String PERMISSION_DENIED = "해당 작업을 수행할 권한이 없습니다.";
    public static final String UNAUTHORIZED_ACTION = "해당 작업에 대한 권한이 없습니다.";
    public static final String PLANT_PERMISSION_DENIED = "식물에 대한 권한이 없습니다.";
    public static final String DIARY_PERMISSION_DENIED = "해당 다이어리에 대한 접근 권한이 없습니다.";
    
    // 인증 관련 에러 메시지
    public static final String AUTHENTICATION_FAILED = "인증에 실패했습니다.";
    public static final String INVALID_CREDENTIALS = "아이디 또는 비밀번호가 올바르지 않습니다.";
    public static final String JWT_EXPIRED = "만료된 JWT 토큰입니다.";
    public static final String JWT_INVALID = "유효하지 않은 JWT 토큰입니다.";
    
    // 요청 관련 에러 메시지
    public static final String BAD_REQUEST = "잘못된 요청입니다.";
    public static final String VALIDATION_FAILED = "입력값 검증에 실패했습니다.";
    public static final String MISSING_PARAMETER = "필수 파라미터가 누락되었습니다.";
    
    // 서버 관련 에러 메시지
    public static final String SERVER_ERROR = "서버 내부 오류가 발생했습니다.";
    public static final String DATABASE_ERROR = "데이터베이스 오류가 발생했습니다.";
    
    // 외부 서비스 관련 에러 메시지
    public static final String EXTERNAL_SERVICE_ERROR = "외부 서비스 연동 중 오류가 발생했습니다.";
    public static final String IMAGE_UPLOAD_ERROR = "이미지 업로드 중 오류가 발생했습니다.";
    public static final String IMAGE_DELETE_ERROR = "이미지 삭제 중 오류가 발생했습니다.";
    
    // 기타 에러 메시지
    public static final String UNEXPECTED_ERROR = "예상치 못한 오류가 발생했습니다.";
    
    // 생성자를 private으로 선언하여 인스턴스화 방지
    private ErrorMessages() {
        throw new AssertionError("ErrorMessages 클래스는 인스턴스화할 수 없습니다.");
    }
} 