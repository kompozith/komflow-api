package com.kompozith.komflow.features.organization.controller;

import com.kompozith.komflow.configuration.security.JwtUtil;
import com.kompozith.komflow.features.organization.TenantContext;
import com.kompozith.komflow.features.organization.dto.*;
import com.kompozith.komflow.features.organization.entity.OrganizationMember;
import com.kompozith.komflow.features.organization.entity.OrganizationMember.MemberStatus;
import com.kompozith.komflow.features.organization.repository.OrganizationMemberRepository;
import com.kompozith.komflow.features.organization.service.WorkspaceMemberService;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Gestion multi-espace et membres")
public class WorkspaceMemberController {

  private final WorkspaceMemberService     memberService;
  private final OrganizationMemberRepository memberRepository;
  private final UserRepository             userRepository;
  private final PersonRepository           personRepository;
  private final JwtUtil                    jwtUtil;

  // ── Mes espaces ─────────────────────────────────────────────────────────

  // ── Créer un espace ──────────────────────────────────────────────────────

  record CreateWorkspaceRequest(String name, String slug) {}
  record CreateWorkspaceResponse(String accessToken, WorkspaceSummaryDto workspace) {}

  @PostMapping("/workspaces")
  @Operation(summary = "Créer un nouvel espace — devient automatiquement OWNER")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CreateWorkspaceResponse> createWorkspace(
      @RequestBody CreateWorkspaceRequest req) {

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = resolveUserByEmail(email);

    WorkspaceSummaryDto ws = memberService.createWorkspace(req.name(), req.slug(), user);
    String newToken = jwtUtil.generateToken(email, "", ws.orgId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new CreateWorkspaceResponse(newToken, ws));
  }

  // ── Mes espaces ──────────────────────────────────────────────────────────

  @GetMapping("/workspaces")
  @Operation(summary = "Tous les espaces auxquels j'appartiens (actifs + invitations)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<WorkspaceSummaryDto>> myWorkspaces() {
    return ResponseEntity.ok(memberService.listMyWorkspaces());
  }

  // ── Switcher d'espace ───────────────────────────────────────────────────

  record SwitchResponse(String accessToken, WorkspaceSummaryDto workspace) {}

  @PostMapping("/workspaces/{orgId}/switch")
  @Operation(summary = "Switcher vers un espace — retourne un nouveau JWT avec le bon organizationId")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<SwitchResponse> switchWorkspace(@PathVariable Long orgId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    User user = resolveUserByEmail(email);

    // Vérifier que le user est membre actif de cet espace
    OrganizationMember member = memberRepository
        .findByOrganizationIdAndUserId(orgId, user.getId())
        .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
        .orElseThrow(() -> new SecurityException("Accès refusé à cet espace."));

    // Nouveau JWT avec le bon organizationId
    String newToken = jwtUtil.generateToken(email, "", orgId);

    WorkspaceSummaryDto summary = WorkspaceSummaryDto.from(member);
    return ResponseEntity.ok(new SwitchResponse(newToken, summary));
  }

  // ── Membres ─────────────────────────────────────────────────────────────

  @GetMapping("/organizations/{orgId}/members")
  @Operation(summary = "Lister les membres d'un espace")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<MemberDto>> listMembers(@PathVariable Long orgId) {
    return ResponseEntity.ok(memberService.listMembers(orgId));
  }

  @PostMapping("/organizations/{orgId}/members/invite")
  @Operation(summary = "Inviter un utilisateur dans l'espace")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MemberDto> inviteMember(
      @PathVariable Long orgId,
      @Valid @RequestBody InviteMemberRequest req) {

    Long callerId = resolveCurrentUserId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(memberService.inviteMember(orgId, req, callerId));
  }

  @PutMapping("/organizations/{orgId}/members/{memberId}")
  @Operation(summary = "Modifier le rôle ou les permissions d'un membre")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MemberDto> updateMember(
      @PathVariable Long orgId,
      @PathVariable Long memberId,
      @RequestBody UpdateMemberRequest req) {

    Long callerId = resolveCurrentUserId();
    return ResponseEntity.ok(memberService.updateMember(orgId, memberId, req, callerId));
  }

  @DeleteMapping("/organizations/{orgId}/members/{memberId}")
  @Operation(summary = "Révoquer l'accès d'un membre")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> revokeMember(
      @PathVariable Long orgId,
      @PathVariable Long memberId) {

    Long callerId = resolveCurrentUserId();
    memberService.revokeMember(orgId, memberId, callerId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/workspaces/{orgId}/leave")
  @Operation(summary = "Quitter un espace (sauf si dernier OWNER)")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> leaveWorkspace(@PathVariable Long orgId) {
    memberService.leaveWorkspace(orgId);
    return ResponseEntity.noContent().build();
  }

  // ── Accepter une invitation ─────────────────────────────────────────────

  record AcceptResponse(String accessToken, WorkspaceSummaryDto workspace) {}

  @PostMapping("/invitations/accept")
  @Operation(summary = "Accepter une invitation par token — retourne un JWT pour le nouvel espace")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AcceptResponse> acceptInvitation(@RequestParam String token) {
    MemberDto member = memberService.acceptInvitation(token);

    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    String newToken = jwtUtil.generateToken(email, "", member.orgId());

    WorkspaceSummaryDto workspace = new WorkspaceSummaryDto(
        member.orgId(), null, null, null,
        member.role(), member.status(),
        member.role() == com.kompozith.komflow.features.organization.entity.OrganizationMember.WorkspaceRole.OWNER
    );

    return ResponseEntity.ok(new AcceptResponse(newToken, workspace));
  }

  // ── Helper ──────────────────────────────────────────────────────────────

  private Long resolveCurrentUserId() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return resolveUserByEmail(email).getId();
  }

  private User resolveUserByEmail(String email) {
    return personRepository.findByEmail(email)
        .flatMap(person -> userRepository.findByPersonId(person.getId()))
        .orElseThrow(() -> new IllegalStateException("User not found: " + email));
  }
}
