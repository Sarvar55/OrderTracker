package com.codems.ordertracker.common.exceptions.handler;


import java.util.LinkedHashMap;
import java.util.Map;

import com.codems.ordertracker.common.exceptions.types.BaseException;
import com.codems.ordertracker.common.exceptions.types.CommonErrorType;
import com.codems.ordertracker.domain.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Object>> handleBaseException(BaseException exception) {
        log.warn("Handled application exception [{}]: {}", exception.getCode(), exception.getMessage());
        return build(
                exception.getCode(),
                exception.getStatus(),
                exception.getMessage(),
                exception.getValidationErrors(),
                exception.getDetails()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("Validation failed with {} field errors", fieldErrors.size());

        return build(CommonErrorType.VALIDATION_FAILED, fieldErrors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Object>> handleAuthentication(AuthenticationException exception) {
        log.debug("Authentication exception handled: {}", exception.getMessage());
        return build(CommonErrorType.UNAUTHORIZED, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleBadCredentials(BadCredentialsException exception) {
        log.warn("Bad credentials handled");
        return build(CommonErrorType.UNAUTHORIZED, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDenied(AccessDeniedException exception) {
        log.debug("Access denied handled");
        return build(CommonErrorType.FORBIDDEN, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = "Invalid value for parameter: " + exception.getName();
        log.warn("Request parameter type mismatch for {}", exception.getName());
        return build(
                CommonErrorType.BAD_REQUEST.code(),
                CommonErrorType.BAD_REQUEST.status(),
                message,
                null,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Database constraint violation: {}", exception.getMostSpecificCause().getMessage());
        return build(CommonErrorType.CONFLICT, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return build(CommonErrorType.INTERNAL_ERROR, null);
    }

    private ResponseEntity<BaseResponse<Object>> build(CommonErrorType errorType, Map<String, String> fieldErrors) {
        return build(errorType.code(), errorType.status(), errorType.message(), fieldErrors, null);
    }

    private ResponseEntity<BaseResponse<Object>> build(
            String code,
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(status)
                .body(BaseResponse.error(code, message, status, fieldErrors, details));
    }
}
