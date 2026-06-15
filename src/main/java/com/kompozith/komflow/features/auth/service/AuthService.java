package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.LoginResponseDto;
import com.kompozith.komflow.features.auth.dto.RefreshTokenDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserPermissionsDto;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.util.SimpleResponse;

public interface AuthService {

    SimpleResponse<UserDetailsDto>  signUp(SignUpDto registerDto);

    /** Inscription SaaS : crée l'organisation + l'utilisateur admin, retourne les JWT. */
    LoginResponseDto signUpForFrontend(SignUpDto signUpDto);

    SimpleResponse<UserDetailsDto> login(LoginDto loginDto);

    SimpleResponse<UserDetailsDto> refreshToken(RefreshTokenDto refreshTokenDto);

    LoginResponseDto loginForFrontend(LoginDto loginDto);

    LoginResponseDto refreshTokenForFrontend(RefreshTokenDto refreshTokenDto);

    void logout();

    UserPermissionsDto getUserPermissions();
}
