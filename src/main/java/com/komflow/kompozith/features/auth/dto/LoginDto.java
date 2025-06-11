package com.komflow.kompozith.features.auth.dto;

import com.komflow.kompozith.features.auth.validation.LoginParameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@LoginParameter
public record LoginDto(

    @Email(message = "user.email.format")
    String email,

    String username,

    @NotBlank(message = "user.password.blank")
    @Size(min = 6, max = 20, message = "user.password.length")
    String password
) {}
