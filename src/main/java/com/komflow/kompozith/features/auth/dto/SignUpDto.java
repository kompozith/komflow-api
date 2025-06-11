package com.komflow.kompozith.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpDto (

    @NotBlank(message = "user.email.blank")
    @Email(message = "user.email.format")
    String email,

    @NotBlank(message = "user.username.blank")
    String username,

    @NotBlank(message = "user.password.blank")
    @Size(min = 6, max = 20, message = "user.password.length")
    String password,

    String firstName,
    String lastName
) {}
