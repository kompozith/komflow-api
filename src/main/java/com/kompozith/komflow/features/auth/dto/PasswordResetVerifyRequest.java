package com.kompozith.komflow.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Étape 2 – vérification OTP : contact (email/phone) + code à 6 chiffres. */
public record PasswordResetVerifyRequest(
        @NotBlank String contact,
        @NotBlank @Pattern(regexp = "\\d{6}") String otpCode
) {}
