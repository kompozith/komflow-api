package com.kompozith.komflow.features.billing.entity;

import java.io.Serializable;
import java.util.Objects;

public class UsageCounterId implements Serializable {
  private Long organizationId;
  private String metric;
  private String yearMonth;

  public UsageCounterId() {}

  public UsageCounterId(Long organizationId, String metric, String yearMonth) {
    this.organizationId = organizationId;
    this.metric = metric;
    this.yearMonth = yearMonth;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UsageCounterId that)) return false;
    return Objects.equals(organizationId, that.organizationId)
        && Objects.equals(metric, that.metric)
        && Objects.equals(yearMonth, that.yearMonth);
  }

  @Override public int hashCode() {
    return Objects.hash(organizationId, metric, yearMonth);
  }
}
