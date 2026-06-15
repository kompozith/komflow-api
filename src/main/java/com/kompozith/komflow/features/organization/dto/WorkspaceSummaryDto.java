package com.kompozith.komflow.features.organization.dto;

import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import com.kompozith.komflow.features.organization.entity.Organization;

public record WorkspaceSummaryDto(
    Long          orgId,
    String        orgName,
    String        orgSlug,
    String        planCode,
    WorkspaceRole myRole,
    MemberStatus  myStatus,
    boolean       isOwner
) {
  public static WorkspaceSummaryDto from(OrganizationMember m) {
    Organization org = m.getOrganization();
    return new WorkspaceSummaryDto(
        org.getId(),
        org.getName(),
        org.getSlug(),
        org.getPlanCode(),
        m.getRole(),
        m.getStatus(),
        m.getRole() == WorkspaceRole.OWNER
    );
  }
}
