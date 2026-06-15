package com.kompozith.komflow.features.billing.repository;

import com.kompozith.komflow.features.billing.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, String> {
}
