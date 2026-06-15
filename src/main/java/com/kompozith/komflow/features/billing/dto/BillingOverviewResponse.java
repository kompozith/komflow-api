package com.kompozith.komflow.features.billing.dto;

import java.util.List;

public record BillingOverviewResponse(
    String planCode,
    String planLabel,
    String subscriptionStatus,
    long   priceMonthlyCtsCents,
    List<QuotaUsage> quotas
) {
  public record QuotaUsage(
      String metric,
      long   used,
      long   limit,   // -1 = illimité
      double pct      // 0.0 – 1.0 (null si illimité)
  ) {}
}
