package com.kompozith.komflow.features.billing.entity;

import com.kompozith.komflow.features.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Abonnement actif d'une organisation à un plan.
 */
@Entity
@Table(name = "bil_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false, unique = true)
  private Organization organization;

  @Column(name = "plan_code", nullable = false, length = 50)
  private String planCode;

  /** ACTIVE | TRIALING | PAST_DUE | CANCELED */
  @Builder.Default
  @Column(nullable = false, length = 30)
  private String status = "ACTIVE";

  /** Fin de la période courante (null = pas d'expiration). */
  private Instant currentPeriodEnd;

  /** ID externe chez l'opérateur de paiement (optionnel). */
  @Column(length = 255)
  private String externalId;

  @Builder.Default
  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @PreUpdate
  private void onUpdate() { /* audit si besoin */ }
}
