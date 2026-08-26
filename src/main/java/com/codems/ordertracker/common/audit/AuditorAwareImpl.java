package com.codems.ordertracker.common.audit;

import com.codems.ordertracker.common.util.ApplicationUtility;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return ApplicationUtility.getCurrentUserId();
    }
}
