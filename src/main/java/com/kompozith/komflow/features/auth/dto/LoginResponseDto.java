package com.kompozith.komflow.features.auth.dto;

import com.kompozith.komflow.features.organization.dto.WorkspaceSummaryDto;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

// DTO for frontend-compatible login response
@Data
@SuperBuilder
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private UserDetailsDto user;
    private UserPermissionsDto permissions;
    /** Workspaces the user belongs to, so the frontend can detect "no workspace" right after login. */
    private List<WorkspaceSummaryDto> workspaces;

    public static LoginResponseDto fromUserDetailsDto(
            UserDetailsDto userDetails,
            String accessToken,
            String refreshToken,
            Integer expiresIn,
            UserPermissionsDto permissions,
            List<WorkspaceSummaryDto> workspaces
    ) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userDetails)
                .permissions(permissions)
                .workspaces(workspaces)
                .build();
    }
}
