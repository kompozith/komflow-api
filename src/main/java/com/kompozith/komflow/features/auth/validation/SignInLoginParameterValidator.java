package com.kompozith.komflow.features.auth.validation;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SignInLoginParameterValidator implements ConstraintValidator<LoginParameter, LoginDto> {

    @Override
    public boolean isValid(LoginDto dto, ConstraintValidatorContext context) {
        return dto != null;
    }
}