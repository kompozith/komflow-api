package com.kompozith.komflow.features.billing.repository;

import com.kompozith.komflow.features.billing.entity.UsageCounter;
import com.kompozith.komflow.features.billing.entity.UsageCounterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsageCounterRepository extends JpaRepository<UsageCounter, UsageCounterId> {

  Optional<UsageCounter> findByOrganizationIdAndMetricAndYearMonth(
      Long organizationId, String metric, String yearMonth);

  List<UsageCounter> findByOrganizationIdAndYearMonth(
      Long organizationId, String yearMonth);

  @Modifying
  @Query("""
      UPDATE UsageCounter u
         SET u.count = u.count + :amount
       WHERE u.organizationId = :orgId
         AND u.metric = :metric
         AND u.yearMonth = :yearMonth
      """)
  int incrementCounter(Long orgId, String metric, String yearMonth, long amount);
}
