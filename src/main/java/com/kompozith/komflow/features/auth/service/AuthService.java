package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.LoginDto;
import com.kompozith.komflow.features.auth.dto.SignUpDto;
import com.kompozith.komflow.features.auth.dto.UserDetailsWithTokenDto;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.configuration.util.SimpleResponse;

public interface AuthService {

    SimpleResponse<UserDetailsDto> signUp(SignUpDto registerDto);

    SimpleResponse<UserDetailsWithTokenDto> login(LoginDto loginDto);
}
