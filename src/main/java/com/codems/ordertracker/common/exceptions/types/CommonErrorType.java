package com.codems.ordertracker.common.exceptions.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorType implements ErrorType {

    VALIDATION_FAILED("COMMON_001", "Validation failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("COMMON_002", "Bad request", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("COMMON_003", "Resource not found", HttpStatus.NOT_FOUND),
    CONFLICT("COMMON_004", "Resource conflict", HttpStatus.CONFLICT),
    UNAUTHORIZED("SECURITY_001", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("SECURITY_002", "Access denied", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED("SECURITY_003", "Your account has been locked", HttpStatus.LOCKED),
    INTERNAL_ERROR("COMMON_999", "Unexpected server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
