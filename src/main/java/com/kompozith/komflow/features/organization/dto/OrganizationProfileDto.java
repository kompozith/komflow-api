package com.kompozith.komflow.features.organization.dto;

import java.time.Instant;

public record OrganizationProfileDto(
    Long    id,
    String  name,
    String  slug,
    String  planCode,
    boolean active,
    Instant trialEndsAt,
    Instant createdAt
) {}
