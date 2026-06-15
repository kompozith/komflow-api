package com.kompozith.komflow.features.organization.dto;

import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
    @NotBlank @Email
    String email,

    @NotNull
    WorkspaceRole role,

    /** Permissions supplémentaires au-delà du rôle (peut être null). */
    java.util.Set<String> extraPermissions
) {}
