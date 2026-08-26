package com.codems.ordertracker.common.exceptions.types;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(CommonErrorType.RESOURCE_NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String message) {
        return new ResourceNotFoundException(message);
    }

    public static ResourceNotFoundException forResource(String resourceName, Object identifier) {
        return new ResourceNotFoundException(resourceName + " not found: " + identifier);
    }
}
