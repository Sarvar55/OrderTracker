package com.codems.ordertracker.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompromisedPasswordValidator implements ConstraintValidator<CompromisedPassword, String> {

    private final CompromisedPasswordChecker compromisedPasswordChecker;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !compromisedPasswordChecker.check(value).isCompromised();
    }
}
