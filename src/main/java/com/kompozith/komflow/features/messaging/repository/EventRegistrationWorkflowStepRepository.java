package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.EventRegistrationWorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRegistrationWorkflowStepRepository extends JpaRepository<EventRegistrationWorkflowStep, Long> {
    List<EventRegistrationWorkflowStep> findAllByEventIdOrderByPositionAsc(Long eventId);
}
