package com.kompozith.komflow.features.auth.dto;

import com.kompozith.komflow.features.auth.validation.LoginParameter;
import jakarta.validation.constraints.NotBlank;

@LoginParameter
public record LoginDto(

    @NotBlank(message = "login.password.blank")
    String login,

    @NotBlank(message = "user.password.blank")
    String password
) {}
