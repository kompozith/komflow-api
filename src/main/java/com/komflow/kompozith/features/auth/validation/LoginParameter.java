package com.komflow.kompozith.features.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SignInLoginParameterValidator.class)
@Documented
public @interface LoginParameter {

    String message() default "atLast.oneField.required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
