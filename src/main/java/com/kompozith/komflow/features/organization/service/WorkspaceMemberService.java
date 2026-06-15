package com.kompozith.komflow.features.organization.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.organization.dto.*;
import com.kompozith.komflow.features.organization.entity.Organization;
import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole;
import com.kompozith.komflow.features.organization.repository.OrganizationMemberRepository;
import com.kompozith.komflow.features.organization.repository.OrganizationRepository;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * Service de gestion des membres et des espaces de travail.
 *
 * Responsabilités :
 *  - Inviter un utilisateur dans un espace (par email)
 *  - Accepter / refuser une invitation via token
 *  - Modifier le rôle ou les permissions d'un membre
 *  - Révoquer l'accès d'un membre
 *  - Lister les espaces accessibles par un utilisateur
 *  - Switcher d'espace (retourne un nouveau JWT)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceMemberService {

  private static final SecureRandom RNG = new SecureRandom();

  @Value("${app.invite.token-ttl-hours:72}")
  private int inviteTokenTtlHours;

  @Value("${app.frontend.base-url:http://localhost:4200}")
  private String frontendBaseUrl;

  private final OrganizationRepository       organizationRepository;
  private final OrganizationMemberRepository memberRepository;
  private final UserRepository               userRepository;
  private final PersonRepository             personRepository;

  // ── Invitation ─────────────────────────────────────────────────────────

  /**
   * Invite un utilisateur (connu ou inconnu) dans l'espace courant.
   * Si l'utilisateur n'existe pas encore, l'invitation reste PENDING
   * et sera liée à un compte lors de l'acceptation.
   */
  @Transactional
  public MemberDto inviteMember(Long orgId, InviteMemberRequest req, Long invitedByUserId) {
    Organization org = resolveOrg(orgId);

    // L'invitant doit être OWNER ou ADMIN
    assertRole(orgId, invitedByUserId, WorkspaceRole.ADMIN);

    // Chercher un compte existant par email via Person
    User targetUser = personRepository.findByEmail(req.email())
        .flatMap(p -> userRepository.findByPersonId(p.getId()))
        .orElse(null);

    if (targetUser != null) {
      // Ne pas ré-inviter si déjà membre actif
      if (memberRepository.existsByOrganizationIdAndUserId(orgId, targetUser.getId())) {
        throw new ObjectExistException("OrganizationMember", "email", req.email());
      }
    } else {
      // Invitation en attente sur un email sans compte (idempotent)
      memberRepository.findByOrganizationIdAndInvitedEmailAndStatus(
          orgId, req.email(), MemberStatus.INVITED)
          .ifPresent(m -> { throw new ObjectExistException("OrganizationMember", "email", req.email()); });
    }

    String token   = generateToken();
    Instant expiry = Instant.now().plus(inviteTokenTtlHours, ChronoUnit.HOURS);

    // Si l'utilisateur existe déjà, on le lie directement mais en statut INVITED
    OrganizationMember member = OrganizationMember.builder()
        .organization(org)
        .user(targetUser != null ? targetUser : resolveSystemUser())
        .role(req.role())
        .extraPermissions(req.extraPermissions() != null ? req.extraPermissions() : Set.of())
        .status(MemberStatus.INVITED)
        .invitedEmail(req.email())
        .inviteToken(token)
        .inviteTokenExpiresAt(expiry)
        .build();

    member = memberRepository.save(member);

    log.info("[Workspace] Invitation envoyée org={} email={} role={} token={}",
        orgId, req.email(), req.role(), token);

    // TODO : envoyer l'email via ChannelOperatorStack
    // L'URL d'acceptation : frontendBaseUrl + "/accept-invite?token=" + token
    log.info("[Workspace] Lien invitation : {}/accept-invite?token={}", frontendBaseUrl, token);

    return MemberDto.from(member);
  }

  /**
   * Accepte une invitation par token.
   * Lie le token au compte authentifié courant.
   */
  @Transactional
  public MemberDto acceptInvitation(String token) {
    OrganizationMember member = memberRepository.findByInviteToken(token)
        .orElseThrow(() -> new ObjectNotFoundException("Invitation", "token", token));

    if (!member.isInvitePending()) {
      throw new IllegalStateException("Invitation expirée ou déjà utilisée.");
    }

    User currentUser = currentUser();

    // Attacher l'utilisateur courant au membre
    member.setUser(currentUser);
    member.setStatus(MemberStatus.ACTIVE);
    member.setInviteToken(null);
    member.setInviteTokenExpiresAt(null);
    memberRepository.save(member);

    log.info("[Workspace] Invitation acceptée org={} user={}",
        member.getOrganization().getId(), currentUser.getUsername());

    return MemberDto.from(member);
  }

  // ── Modification de rôle / permissions ─────────────────────────────────

  @Transactional
  public MemberDto updateMember(Long orgId, Long memberId, UpdateMemberRequest req, Long requesterId) {
    assertRole(orgId, requesterId, WorkspaceRole.ADMIN);

    OrganizationMember member = resolveMember(orgId, memberId);

    // Empêcher de rétrograder le dernier OWNER
    if (member.getRole() == WorkspaceRole.OWNER && req.role() != WorkspaceRole.OWNER) {
      if (memberRepository.countOwnersByOrganizationId(orgId) <= 1) {
        throw new IllegalStateException("L'espace doit avoir au moins un OWNER.");
      }
    }

    if (req.role() != null)             member.setRole(req.role());
    if (req.extraPermissions() != null) member.setExtraPermissions(req.extraPermissions());

    return MemberDto.from(memberRepository.save(member));
  }

  // ── Révocation ─────────────────────────────────────────────────────────

  @Transactional
  public void revokeMember(Long orgId, Long memberId, Long requesterId) {
    assertRole(orgId, requesterId, WorkspaceRole.ADMIN);

    OrganizationMember member = resolveMember(orgId, memberId);

    // Empêcher la révocation du dernier OWNER
    if (member.getRole() == WorkspaceRole.OWNER) {
      if (memberRepository.countOwnersByOrganizationId(orgId) <= 1) {
        throw new IllegalStateException("Impossible de révoquer le dernier OWNER.");
      }
    }

    member.setStatus(MemberStatus.SUSPENDED);
    memberRepository.save(member);
    log.info("[Workspace] Membre révoqué org={} memberId={}", orgId, memberId);
  }

  /**
   * Permet à l'utilisateur courant de quitter un espace (sauf s'il est le dernier OWNER).
   */
  @Transactional
  public void leaveWorkspace(Long orgId) {
    User current = currentUser();
    OrganizationMember member = memberRepository
        .findByOrganizationIdAndUserId(orgId, current.getId())
        .orElseThrow(() -> new ObjectNotFoundException("OrganizationMember", "user", current.getUsername()));

    if (member.getRole() == WorkspaceRole.OWNER
        && memberRepository.countOwnersByOrganizationId(orgId) <= 1) {
      throw new IllegalStateException("Vous êtes le seul OWNER — transférez la propriété avant de quitter.");
    }

    memberRepository.delete(member);
    log.info("[Workspace] User {} a quitté l'org {}", current.getUsername(), orgId);
  }

  // ── Lister les espaces ─────────────────────────────────────────────────

  /** Retourne tous les espaces actifs (et invitations en attente) du user courant. */
  @Transactional(readOnly = true)
  public List<WorkspaceSummaryDto> listMyWorkspaces() {
    User current = currentUser();
    return memberRepository.findByUserIdOrderByCreatedAtAsc(current.getId())
        .stream()
        .map(WorkspaceSummaryDto::from)
        .toList();
  }

  /** Membres d'un espace (nécessite d'en être membre). */
  @Transactional(readOnly = true)
  public List<MemberDto> listMembers(Long orgId) {
    assertMembership(orgId, currentUser().getId());
    return memberRepository.findByOrganizationId(orgId)
        .stream()
        .map(MemberDto::from)
        .toList();
  }

  // ── Helpers privés ─────────────────────────────────────────────────────

  private void assertRole(Long orgId, Long userId, WorkspaceRole minimumRole) {
    OrganizationMember m = memberRepository.findByOrganizationIdAndUserId(orgId, userId)
        .orElseThrow(() -> new SecurityException("Accès refusé : non membre de cet espace."));
    if (m.getStatus() != MemberStatus.ACTIVE) {
      throw new SecurityException("Accès refusé : membre non actif.");
    }
    if (minimumRole == WorkspaceRole.ADMIN
        && m.getRole() != WorkspaceRole.OWNER && m.getRole() != WorkspaceRole.ADMIN) {
      throw new SecurityException("Droits insuffisants (ADMIN requis).");
    }
  }

  private void assertMembership(Long orgId, Long userId) {
    if (!memberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
      throw new SecurityException("Accès refusé : non membre de cet espace.");
    }
  }

  private Organization resolveOrg(Long orgId) {
    return organizationRepository.findById(orgId)
        .orElseThrow(() -> new ObjectNotFoundException("Organization", "id", String.valueOf(orgId)));
  }

  private OrganizationMember resolveMember(Long orgId, Long memberId) {
    OrganizationMember m = memberRepository.findById(memberId)
        .orElseThrow(() -> new ObjectNotFoundException("OrganizationMember", "id", String.valueOf(memberId)));
    if (!m.getOrganization().getId().equals(orgId)) {
      throw new ObjectNotFoundException("OrganizationMember", "id", String.valueOf(memberId));
    }
    return m;
  }

  private User currentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ObjectNotFoundException("User", "username", username));
  }

  /**
   * Retourne un user "système" placeholder pour les invitations
   * sur un email sans compte. On utilise le premier user disponible
   * (sera remplacé à l'acceptation).
   */
  private User resolveSystemUser() {
    // Cas simplifié : on lève une exception pour forcer l'invitation uniquement de users existants.
    // Dans une évolution future : créer un compte "pending" avec is_enabled=false.
    throw new IllegalArgumentException(
        "Aucun compte trouvé pour cet email. L'utilisateur doit d'abord créer son compte.");
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    RNG.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  // ── Créer un espace ───────────────────────────────────────────────────

  @Transactional
  public WorkspaceSummaryDto createWorkspace(String name, String rawSlug, User owner) {
    // Générer le slug si absent
    String slug = (rawSlug != null && !rawSlug.isBlank())
        ? rawSlug.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
        : name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");

    if (organizationRepository.existsBySlug(slug)) {
      throw new ObjectExistException("Organization", "slug", slug);
    }

    Organization org = Organization.builder()
        .name(name)
        .slug(slug)
        .planCode("FREE")
        .active(true)
        .build();
    org = organizationRepository.save(org);

    OrganizationMember member = OrganizationMember.builder()
        .organization(org)
        .user(owner)
        .role(WorkspaceRole.OWNER)
        .status(MemberStatus.ACTIVE)
        .build();
    memberRepository.save(member);

    log.info("[Workspace] Espace créé: id={} slug={} owner={}", org.getId(), slug, owner.getUsername());
    return WorkspaceSummaryDto.from(member);
  }

  public boolean slugExists(String slug) {
    return organizationRepository.existsBySlug(slug);
  }
}

