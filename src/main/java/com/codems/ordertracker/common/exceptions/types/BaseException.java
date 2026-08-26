package com.codems.ordertracker.common.exceptions.types;

import org.springframework.http.HttpStatus;

import java.util.Map;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final Map<String, String> validationErrors;
    private final Map<String, Object> details;

    protected BaseException(String message, HttpStatus status) {
        this(CommonErrorType.BAD_REQUEST.code(), message, status, null, null);
    }

    protected BaseException(String message, HttpStatus status, Map<String, String> validationErrors) {
        this(CommonErrorType.VALIDATION_FAILED.code(), message, status, validationErrors, null);
    }

    protected BaseException(ErrorType errorType) {
        this(errorType, errorType.message(), null, null);
    }

    protected BaseException(ErrorType errorType, String message) {
        this(errorType, message, null, null);
    }

    protected BaseException(ErrorType errorType, Map<String, String> validationErrors) {
        this(errorType, errorType.message(), validationErrors, null);
    }

    protected BaseException(
            ErrorType errorType,
            String message,
            Map<String, String> validationErrors,
            Map<String, Object> details
    ) {
        this(errorType.code(), message, errorType.status(), validationErrors, details);
    }

    private BaseException(
            String code,
            String message,
            HttpStatus status,
            Map<String, String> validationErrors,
            Map<String, Object> details
    ) {
        super(message);
        this.code = code;
        this.status = status;
        this.validationErrors = validationErrors;
        this.details = details;
    }

    protected BaseException(String message) {
        super(message);
        this.code = CommonErrorType.BAD_REQUEST.code();
        this.status = HttpStatus.BAD_REQUEST;
        this.validationErrors = null;
        this.details = null;
    }

    public static BaseException of(ErrorType errorType) {
        return new BaseException(errorType);
    }

    public static BaseException of(ErrorType errorType, String message) {
        return new BaseException(errorType, message);
    }
}
