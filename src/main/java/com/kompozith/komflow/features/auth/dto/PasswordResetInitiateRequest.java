package com.kompozith.komflow.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Étape 1 – demande de réinitialisation : email ou numéro de téléphone. */
public record PasswordResetInitiateRequest(
        @NotBlank String contact,
        String contactType   // "EMAIL" | "PHONE" — seul EMAIL est géré actuellement
) {}
