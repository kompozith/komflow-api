package com.komflow.kompozith.features.auth.service;

import com.komflow.kompozith.features.auth.dto.LoginDto;
import com.komflow.kompozith.features.auth.dto.SignUpDto;
import com.komflow.kompozith.features.auth.dto.UserDetailsWithTokenDto;
import com.komflow.kompozith.features.personnel.dto.UserDetailsDto;
import com.komflow.kompozith.features.core.util.SimpleResponse;

public interface AuthService {

    SimpleResponse<UserDetailsDto> signUp(SignUpDto registerDto);

    SimpleResponse<UserDetailsWithTokenDto> login(LoginDto loginDto);
}
