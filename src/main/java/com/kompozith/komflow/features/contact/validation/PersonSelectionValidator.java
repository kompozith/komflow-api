package com.kompozith.komflow.features.contact.validation;

import com.kompozith.komflow.features.contact.dto.CreateContactDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PersonSelectionValidator implements ConstraintValidator<ValidPersonSelection, CreateContactDto> {

    @Override
    public boolean isValid(CreateContactDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        boolean hasPersonId = dto.getPersonId() != null;
        boolean hasPerson = dto.getPerson() != null;

        // Exactly one must be provided (XOR).
        if (hasPersonId == hasPerson) {
            String message = context.getDefaultConstraintMessageTemplate();
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("personId")
                .addConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("person")
                .addConstraintViolation();
            return false;
        }

        return true;
    }
}
