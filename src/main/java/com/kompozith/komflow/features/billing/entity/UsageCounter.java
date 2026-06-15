package com.kompozith.komflow.features.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.YearMonth;

/**
 * Compteur d'usage mensuel par organisation et par métrique.
 * Clé composée : (organizationId, metric, yearMonth).
 */
@Entity
@Table(
  name = "bil_usage_counters",
  uniqueConstraints = @UniqueConstraint(
    columnNames = {"organization_id", "metric", "year_month"}
  )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UsageCounterId.class)
public class UsageCounter {

  @Id
  @Column(name = "organization_id", nullable = false)
  private Long organizationId;

  /** EMAIL | SMS | WHATSAPP | CAMPAIGNS | CONTACTS */
  @Id
  @Column(nullable = false, length = 50)
  private String metric;

  /** Format YYYYMM (ex : 202506) */
  @Id
  @Column(name = "year_month", nullable = false, length = 6)
  private String yearMonth;

  @Builder.Default
  @Column(nullable = false)
  private long count = 0L;

  public void increment(long amount) {
    this.count += amount;
  }

  public static String currentYearMonth() {
    YearMonth ym = YearMonth.now();
    return String.format("%04d%02d", ym.getYear(), ym.getMonthValue());
  }
}
