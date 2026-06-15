package com.kompozith.komflow.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Étape 3 – changement de mot de passe avec le reset token. */
public record PasswordResetCompleteRequest(
        @NotBlank String resetToken,
        @NotBlank @Size(min = 8) String newPassword
) {}
