package com.kompozith.komflow.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpDto (

    @NotBlank(message = "user.email.blank")
    @Email(message = "user.email.format")
    String email,

    @NotBlank(message = "user.password.blank")
    @Size(min = 8, max = 20, message = "user.password.length")
    /**
     * Requires at least one lowercase letter, one uppercase letter, one digit,
     * and one special character from a fixed allow-list (@_#$%-^*()!) — kept
     * in sync with the frontend's PASSWORD_SPECIAL_CHARS. This is a
     * defense-in-depth password-quality rule, not the SQL-injection defense:
     * that is provided by JPA/Hibernate parameterized queries, never by
     * filtering input characters.
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@_#$%\\-^*()!]).+$", message = "user.password.weak")
    String password,

    String firstName,
    String lastName,

    /** Nom de l'organisation à créer. Obligatoire pour l'auto-onboarding SaaS. */
    @NotBlank(message = "organization.name.blank")
    String organizationName,

    /**
     * Slug technique de l'organisation (URL-friendly).
     * Si absent, généré automatiquement depuis {@code organizationName}.
     * Autorisé : lettres minuscules, chiffres et tirets.
     */
    @Pattern(regexp = "^[a-z0-9-]*$", message = "organization.slug.format")
    String organizationSlug
) {}
