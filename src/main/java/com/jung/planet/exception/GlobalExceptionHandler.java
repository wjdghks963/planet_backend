package com.jung.planet.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {


    // EntityNotFoundException에 대한 처리
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", e.getMessage());
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseException(DataAccessException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseBody.put("error", "Database Error");
        responseBody.put("message", "서버 에러");

        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", HttpStatus.FORBIDDEN.value());
        responseBody.put("error", "Expired Refresh JWT");
        responseBody.put("message", "Refresh JWT Token has expired");

        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);
    }

    // MethodArgumentNotValidException에 대한 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("errors", fieldErrors.stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", e.getMessage());
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<?> handleUnauthorizedActionException(UnauthorizedActionException e) {


        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("message", e.getMessage());

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", HttpStatus.FORBIDDEN.value());
        responseBody.put("error", "Unauthorized Action");

        responseBody.put("errors", errors);


        return new ResponseEntity<>(responseBody, HttpStatus.FORBIDDEN);


    }


}
