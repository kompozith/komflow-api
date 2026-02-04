package com.kompozith.komflow.features.contact.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonSelectionValidator.class)
@Documented
public @interface ValidPersonSelection {
    String message() default "contact.person.selection.invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}