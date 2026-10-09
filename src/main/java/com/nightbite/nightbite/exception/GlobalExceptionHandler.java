package com.nightbite.nightbite.exception;

import com.nightbite.nightbite.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exc) {
        String msg = exc.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "Validation error", msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exc) {
        return build(HttpStatus.BAD_REQUEST, "Validation error", exc.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException exc) {
        HttpStatus status = exc.getMessage() != null && (exc.getMessage().toLowerCase().contains("slot") || exc.getMessage().toLowerCase().contains("conflict") || exc.getMessage().toLowerCase().contains("closed"))
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
        return build(status, status.getReasonPhrase(), exc.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exc) {
        return build(HttpStatus.NOT_FOUND, "Not found", exc.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException exc) {
        return build(HttpStatus.CONFLICT, "Conflict", "The requested batch or data already exists");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException exc) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", exc.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exc) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", exc.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception exc) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exc.getMessage());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), error, message, LocalDateTime.now()));
    }
}
