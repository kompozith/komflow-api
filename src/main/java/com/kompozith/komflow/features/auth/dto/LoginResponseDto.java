package com.kompozith.komflow.features.auth.dto;

import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

// DTO for frontend-compatible login response
@Data
@SuperBuilder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private UserDetailsDto user;

    public static LoginResponseDto fromUserDetailsDto(UserDetailsDto userDetails, String accessToken, String refreshToken, Integer expiresIn) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userDetails)
                .build();
    }
}
