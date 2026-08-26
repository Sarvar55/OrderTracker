package com.codems.ordertracker.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CompromisedPasswordValidator.class)
public @interface CompromisedPassword {

    String message() default "Password has been compromised";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
