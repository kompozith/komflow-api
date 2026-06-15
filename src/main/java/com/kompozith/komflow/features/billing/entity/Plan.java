package com.kompozith.komflow.features.billing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Définition d'un plan tarifaire (FREE, STARTER, PRO, ENTERPRISE).
 * Les limites à -1 signifient "illimité".
 */
@Entity
@Table(name = "bil_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

  @Id
  @Column(length = 50)
  private String code; // FREE, STARTER, PRO, ENTERPRISE

  @Column(nullable = false, length = 100)
  private String label;

  /** Prix mensuel en centimes (0 = gratuit). */
  @Builder.Default
  private long priceMonthlyCtsCents = 0L;

  // ── Limites mensuelles (-1 = illimité) ──────────────────────────────────────

  @Builder.Default private int maxContactsTotal      = 500;
  @Builder.Default private int maxCampaignsPerMonth  = 2;
  @Builder.Default private int maxEmailsPerMonth     = 1000;
  @Builder.Default private int maxSmsPerMonth        = 0;
  @Builder.Default private int maxWhatsappPerMonth   = 0;
  @Builder.Default private int maxUsersPerOrg        = 1;

  @Builder.Default private boolean canUseWhatsapp    = false;
  @Builder.Default private boolean canUseSms         = false;
  @Builder.Default private boolean canUseWorkflows   = false;
  @Builder.Default private boolean canUseAdvancedRbac = false;
}
