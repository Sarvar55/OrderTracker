package com.codems.ordertracker.common.util;

import com.codems.ordertracker.common.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class ApplicationUtility {

    private ApplicationUtility() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Optional<SecurityUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return Optional.of(securityUser);
        }

        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(SecurityUser::getUserId);
    }
}
