package com.kompozith.komflow.features.organization.repository;

import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

  /** Tous les espaces auxquels appartient un user (actif ou invité). */
  List<OrganizationMember> findByUserIdOrderByCreatedAtAsc(Long userId);

  /** Membres actifs d'une organisation. */
  List<OrganizationMember> findByOrganizationIdAndStatus(Long orgId, MemberStatus status);

  /** Tous les membres d'une organisation (tous statuts). */
  List<OrganizationMember> findByOrganizationId(Long orgId);

  /** Appartenance spécifique user ↔ org. */
  Optional<OrganizationMember> findByOrganizationIdAndUserId(Long orgId, Long userId);

  /** Invitation par token. */
  Optional<OrganizationMember> findByInviteToken(String inviteToken);

  /** Invitation en attente sur un email dans une org. */
  Optional<OrganizationMember> findByOrganizationIdAndInvitedEmailAndStatus(
      Long orgId, String email, MemberStatus status);

  boolean existsByOrganizationIdAndUserId(Long orgId, Long userId);

  /** Compte des OWNER actifs (pour empêcher de se retirer seul). */
  @Query("""
      SELECT COUNT(m) FROM OrganizationMember m
       WHERE m.organization.id = :orgId
         AND m.role = com.kompozith.komflow.features.organization.entity.OrganizationMember$WorkspaceRole.OWNER
         AND m.status = com.kompozith.komflow.features.organization.entity.OrganizationMember$MemberStatus.ACTIVE
      """)
  long countOwnersByOrganizationId(Long orgId);
}
