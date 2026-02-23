package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.auth.dto.AuditLogListItemDto;
import com.kompozith.komflow.features.auth.entity.AuditLog;
import com.kompozith.komflow.features.auth.repository.AuditLogRepository;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public Page<AuditLogListItemDto> findAll(
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
    ) {
        Pageable normalizedPageable = normalizePageable(pageable);

        List<String> connectedUserIds = resolveConnectedUserIds(userId, username);
        boolean applyUserFilter = !connectedUserIds.isEmpty() || hasText(userId) || hasText(username);
        if (applyUserFilter && connectedUserIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), normalizedPageable, 0);
        }

        Specification<AuditLog> specification = buildSpecification(
                trimToNull(action),
                trimToNull(resource),
                trimToNull(resourceId),
                dateFrom,
                dateTo,
                applyUserFilter,
                connectedUserIds
        );

        Page<AuditLog> page = auditLogRepository.findAll(specification, normalizedPageable);

        Map<String, String> usernamesById = resolveUsernamesByConnectedUserId(page.getContent());
        return page.map(log -> toDto(log, usernamesById));
    }

    @Override
    public AuditLogListItemDto findById(Long id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("AuditLog", id));
        Map<String, String> usernamesById = resolveUsernamesByConnectedUserId(List.of(log));
        return toDto(log, usernamesById);
    }

    private AuditLogListItemDto toDto(AuditLog log, Map<String, String> usernamesById) {
        String connectedUserId = trimToNull(log.getConnectedUserId());
        return AuditLogListItemDto.builder()
                .id(log.getId())
                .timestamp(log.getDate())
                .userId(connectedUserId)
                .username(connectedUserId != null ? usernamesById.getOrDefault(connectedUserId, connectedUserId) : "System")
                .action(log.getAction())
                .resource(log.getObjectType())
                .resourceId(log.getObjectId())
                .success(inferSuccess(log))
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .details(log.getDetails())
                .channel(log.getChannel())
                .userLocation(log.getUserLocation())
                .build();
    }

    private boolean inferSuccess(AuditLog log) {
        String action = safeLower(log.getAction());
        String details = safeLower(log.getDetails());
        return !(action.contains("fail")
                || details.contains("fail")
                || details.contains("error")
                || details.contains("exception"));
    }

    private Map<String, String> resolveUsernamesByConnectedUserId(List<AuditLog> logs) {
        Set<Long> userIds = logs.stream()
                .map(AuditLog::getConnectedUserId)
                .map(this::parseUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userRepository.findAllById(userIds);
        Map<String, String> usernamesById = new HashMap<>();
        for (User user : users) {
            usernamesById.put(String.valueOf(user.getId()), user.getUsername());
        }
        return usernamesById;
    }

    private List<String> resolveConnectedUserIds(String userId, String username) {
        if (hasText(userId)) {
            return List.of(userId.trim());
        }
        if (!hasText(username)) {
            return Collections.emptyList();
        }
        return userRepository.findByUsernameContainingIgnoreCase(username.trim()).stream()
                .map(User::getId)
                .map(String::valueOf)
                .toList();
    }

    private Specification<AuditLog> buildSpecification(
            String action,
            String resource,
            String resourceId,
            Instant dateFrom,
            Instant dateTo,
            boolean applyUserFilter,
            List<String> connectedUserIds
    ) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (hasText(resource)) {
                predicates.add(cb.equal(root.get("objectType"), resource));
            }
            if (hasText(resourceId)) {
                predicates.add(cb.equal(root.get("objectId"), resourceId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            if (applyUserFilter) {
                predicates.add(root.get("connectedUserId").in(connectedUserIds));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Pageable normalizePageable(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }
        List<Sort.Order> normalizedOrders = pageable.getSort().stream()
                .map(order -> new Sort.Order(order.getDirection(), mapSortProperty(order.getProperty())))
                .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(normalizedOrders));
    }

    private String mapSortProperty(String property) {
        if (property == null) {
            return "date";
        }
        return switch (property) {
            case "timestamp" -> "date";
            case "resource" -> "objectType";
            case "resourceId" -> "objectId";
            case "userId", "username" -> "connectedUserId";
            default -> property;
        };
    }

    private Long parseUserId(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
