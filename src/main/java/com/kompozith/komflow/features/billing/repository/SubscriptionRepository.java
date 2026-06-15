package com.kompozith.komflow.features.billing.repository;

import com.kompozith.komflow.features.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  Optional<Subscription> findByOrganizationId(Long organizationId);
}
