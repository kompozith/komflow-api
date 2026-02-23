package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.AuditLogListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditLogService {
    Page<AuditLogListItemDto> findAll(
            Pageable pageable,
            String userId,
            String username,
            String action,
            String resource,
            String resourceId,
            Instant dateFrom,
            Instant dateTo,
            String ipAddress,
            String search
    );

    AuditLogListItemDto findById(Long id);
}
