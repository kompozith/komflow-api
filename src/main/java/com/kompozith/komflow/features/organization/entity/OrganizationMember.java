package com.kompozith.komflow.features.organization.entity;

import com.kompozith.komflow.features.core.entity.BaseEntity;
import com.kompozith.komflow.features.personnel.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Appartenance d'un utilisateur à une organisation.
 *
 * Modèle multi-espace :
 *  - Un user peut être OWNER de son propre espace
 *  - Il peut être invité sur d'autres espaces avec un rôle différent
 *  - Les permissions sur chaque espace sont indépendantes
 *
 * Statuts :
 *  INVITED   → invitation envoyée, user n'a pas encore accepté
 *  ACTIVE    → membre actif
 *  SUSPENDED → accès révoqué temporairement
 */
@Entity
@Table(
  name = "org_members",
  uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrganizationMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_member_seq")
  @SequenceGenerator(name = "org_member_seq", sequenceName = "org_members_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * Rôle de l'utilisateur dans cet espace.
   * OWNER   → créateur, droits complets, non révocable
   * ADMIN   → droits complets sauf supprimer l'espace
   * MEMBER  → droits opérationnels (campagnes, contacts…)
   * VIEWER  → lecture seule
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private WorkspaceRole role = WorkspaceRole.MEMBER;

  /**
   * Permissions supplémentaires accordées manuellement, en plus de celles du rôle.
   * Utilise les codes de permission existants (ex: "CONTACT_CREATE").
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "org_member_permissions", joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "permission", length = 100)
  @Builder.Default
  private Set<String> extraPermissions = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private MemberStatus status = MemberStatus.ACTIVE;

  /** Email qui a été invité (peut différer si le user change son email). */
  @Column(name = "invited_email", length = 255)
  private String invitedEmail;

  /** Token opaque pour accepter l'invitation par email (null si déjà accepté). */
  @Column(name = "invite_token", unique = true, length = 64)
  private String inviteToken;

  /** Expiration du token d'invitation. */
  @Column(name = "invite_token_expires_at")
  private Instant inviteTokenExpiresAt;

  // ── Enums inline ─────────────────────────────────────────────────────────

  public enum WorkspaceRole {
    OWNER, ADMIN, MEMBER, VIEWER
  }

  public enum MemberStatus {
    INVITED, ACTIVE, SUSPENDED
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  public boolean isInvitePending() {
    return status == MemberStatus.INVITED
        && inviteTokenExpiresAt != null
        && inviteTokenExpiresAt.isAfter(Instant.now());
  }
}
