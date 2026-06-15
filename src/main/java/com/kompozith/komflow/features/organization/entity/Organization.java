package com.kompozith.komflow.features.organization.entity;

import com.kompozith.komflow.features.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Représente une organisation (tenant) dans le modèle multi-tenant.
 * Chaque ressource métier (contacts, messages, campagnes) sera rattachée
 * à une organisation via une colonne organization_id.
 */
@Entity
@Table(name = "org_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Organization extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq")
  @SequenceGenerator(name = "org_seq", sequenceName = "org_organizations_id_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(unique = true, nullable = false, length = 100)
  private String slug;

  /** Code du plan souscrit (FREE, STARTER, PRO, ENTERPRISE). */
  @Column(nullable = false, length = 50)
  @Builder.Default
  private String planCode = "FREE";

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  /** Date de fin de la période d'essai (null = pas d'essai actif). */
  @Column(name = "trial_ends_at")
  private Instant trialEndsAt;
}
