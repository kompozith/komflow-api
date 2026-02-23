package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.AuditLogListItemDto;
import com.kompozith.komflow.features.auth.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyAuthority('AUDIT_VIEW', 'AUDIT_LIST', 'PERSONNEL_VIEW')")
    @GetMapping
    public ResponseEntity<Page<AuditLogListItemDto>> findAll(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(auditLogService.findAll(
                pageable,
                userId,
                username,
                action,
                resource,
                resourceId,
                dateFrom,
                dateTo,
                ipAddress,
                search
        ));
    }

    @PreAuthorize("hasAnyAuthority('AUDIT_VIEW', 'AUDIT_LIST', 'PERSONNEL_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogListItemDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }
}
