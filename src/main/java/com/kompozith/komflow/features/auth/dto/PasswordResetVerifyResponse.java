package com.kompozith.komflow.features.auth.dto;

/** Réponse à l'étape 2 – contient le token long pour l'étape 3. */
public record PasswordResetVerifyResponse(String resetToken) {}
