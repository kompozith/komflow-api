package com.kompozith.komflow.features.organization.dto;

import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import java.util.Set;

public record UpdateMemberRequest(
    WorkspaceRole role,
    Set<String>   extraPermissions
) {}
