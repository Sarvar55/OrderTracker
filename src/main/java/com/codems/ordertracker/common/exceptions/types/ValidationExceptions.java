package com.codems.ordertracker.common.exceptions.types;

import java.util.Map;

public class ValidationExceptions extends BaseException {

    public ValidationExceptions(Map<String, String> validationErrors) {
        super(CommonErrorType.VALIDATION_FAILED, validationErrors);
    }

    public static ValidationExceptions from(Map<String, String> validationErrors) {
        return new ValidationExceptions(validationErrors);
    }
}
