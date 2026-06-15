package com.kompozith.komflow.features.organization.controller;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.features.organization.TenantContext;
import com.kompozith.komflow.features.organization.dto.OrganizationProfileDto;
import com.kompozith.komflow.features.organization.dto.UpdateOrganizationRequest;
import com.kompozith.komflow.features.organization.entity.Organization;
import com.kompozith.komflow.features.organization.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Gestion de l'organisation courante")
public class OrganizationController {

  private final OrganizationRepository organizationRepository;

  @GetMapping("/me")
  @Operation(summary = "Profil de l'organisation courante")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<OrganizationProfileDto> getMyOrganization() {
    Organization org = resolveCurrentOrg();
    return ResponseEntity.ok(toDto(org));
  }

  @PutMapping("/me")
  @Operation(summary = "Mettre à jour le nom et le slug de l'organisation")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<OrganizationProfileDto> updateMyOrganization(
      @Valid @RequestBody UpdateOrganizationRequest req) {

    Organization org = resolveCurrentOrg();

    // Vérification de l'unicité du slug si changé
    if (req.slug() != null && !req.slug().isBlank() && !req.slug().equals(org.getSlug())) {
      if (organizationRepository.existsBySlug(req.slug())) {
        throw new ObjectExistException(Organization.class.getSimpleName(), "slug", req.slug());
      }
      org.setSlug(req.slug());
    }

    org.setName(req.name());
    org = organizationRepository.save(org);
    return ResponseEntity.ok(toDto(org));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private Organization resolveCurrentOrg() {
    Long orgId = TenantContext.getOrganizationId();
    if (orgId == null) {
      throw new ObjectNotFoundException(Organization.class.getSimpleName(), "id", "null");
    }
    return organizationRepository.findById(orgId)
        .orElseThrow(() -> new ObjectNotFoundException(Organization.class.getSimpleName(), "id", String.valueOf(orgId)));
  }

  private OrganizationProfileDto toDto(Organization org) {
    return new OrganizationProfileDto(
        org.getId(),
        org.getName(),
        org.getSlug(),
        org.getPlanCode(),
        org.isActive(),
        org.getTrialEndsAt(),
        org.getCreatedAt()
    );
  }
}
