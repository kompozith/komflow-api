package com.kompozith.komflow.features.organization.dto;

import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;

import java.time.Instant;
import java.util.Set;

public record MemberDto(
    Long          id,
    Long          orgId,
    Long          userId,
    String        email,
    String        firstName,
    String        lastName,
    WorkspaceRole role,
    MemberStatus  status,
    Set<String>   extraPermissions,
    String        invitedEmail,
    Instant       createdAt
) {
  public static MemberDto from(OrganizationMember m) {
    var user   = m.getUser();
    var person = user != null ? user.getPerson() : null;
    Long orgId = m.getOrganization() != null ? m.getOrganization().getId() : null;
    return new MemberDto(
        m.getId(),
        orgId,
        user != null ? user.getId() : null,
        person != null ? person.getEmail()     : null,
        person != null ? person.getFirstName() : null,
        person != null ? person.getLastName()  : null,
        m.getRole(),
        m.getStatus(),
        m.getExtraPermissions(),
        m.getInvitedEmail(),
        m.getCreatedAt()
    );
  }
}
