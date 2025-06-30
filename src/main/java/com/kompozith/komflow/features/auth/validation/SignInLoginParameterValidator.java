package com.kompozith.komflow.features.auth.validation;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SignInLoginParameterValidator implements ConstraintValidator<LoginParameter, LoginDto> {

    @Override
    public boolean isValid(LoginDto dto, ConstraintValidatorContext context) {
        if (dto == null) return false;

        /*boolean emailPresent = dto.email() != null && !dto.email().isBlank();
        boolean usernamePresent = dto.username() != null && !dto.username().isBlank();

        return emailPresent || usernamePresent;*/

        return true;
    }
}