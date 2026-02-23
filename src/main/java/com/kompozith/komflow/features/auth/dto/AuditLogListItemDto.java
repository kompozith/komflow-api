package com.kompozith.komflow.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogListItemDto {
    private Long id;
    private Instant timestamp;
    private String userId;
    private String username;
    private String action;
    private String resource;
    private String resourceId;
    private boolean success;
    private String ipAddress;
    private String userAgent;
    private String details;
    private String channel;
    private String userLocation;
}
