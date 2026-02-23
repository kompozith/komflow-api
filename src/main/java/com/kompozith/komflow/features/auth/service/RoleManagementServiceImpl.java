package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.auth.dto.PermissionDto;
import com.kompozith.komflow.features.auth.dto.RoleDto;
import com.kompozith.komflow.features.auth.entity.Role;
import com.kompozith.komflow.features.auth.entity.RoleType;
import com.kompozith.komflow.features.auth.repository.RoleRepository;
import com.kompozith.komflow.features.core.util.PermissionService;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kompozith.komflow.features.core.util.AppConstants.ADMIN_ROLE_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> listRoles(int page, int size, String search, String sortBy, String sortDirection) {
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sortField));

        return roleRepository.search(search, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Role.class.getSimpleName(), id));
        return mapToDto(role);
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        String normalizedName = normalizeName(roleDto.getName());
        if (roleRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ObjectExistException(Role.class.getSimpleName(), "name", normalizedName);
        }

        if (roleDto.getType() == RoleType.SYSTEM) {
            throw new IllegalArgumentException("System roles cannot be created via API");
        }

        Set<String> permissionCodes = sanitizeAndValidatePermissions(roleDto.getPermissionCodeList());

        Role role = new Role();
        role.setName(normalizedName);
        role.setDescription(roleDto.getDescription());
        role.setType(RoleType.CUSTOM);
        role.setActive(true);
        role.setPermissions(permissionCodes);

        return mapToDto(roleRepository.save(role));
    }

    @Override
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Role.class.getSimpleName(), id));
        assertRoleIsMutable(role);

        if (StringUtils.hasText(roleDto.getName())) {
            String normalizedName = normalizeName(roleDto.getName());
            roleRepository.findByNameIgnoreCase(normalizedName)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ObjectExistException(Role.class.getSimpleName(), "name", normalizedName);
                    });
            role.setName(normalizedName);
        }

        role.setDescription(roleDto.getDescription());
        if (roleDto.getType() == RoleType.SYSTEM) {
            throw new IllegalArgumentException("System roles cannot be modified via API");
        }
        if (roleDto.getType() != null) {
            role.setType(RoleType.CUSTOM);
        }

        if (roleDto.getPermissionCodeList() != null) {
            role.setPermissions(sanitizeAndValidatePermissions(roleDto.getPermissionCodeList()));
        }
        if (roleDto.getActive() != null) {
            role.setActive(roleDto.getActive());
        }

        return mapToDto(roleRepository.save(role));
    }

    @Override
    public RoleDto updateRolePermissions(Long id, List<String> permissionCodeList) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Role.class.getSimpleName(), id));
        assertRoleIsMutable(role);

        role.setPermissions(sanitizeAndValidatePermissions(permissionCodeList));
        return mapToDto(roleRepository.save(role));
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Role.class.getSimpleName(), id));

        assertRoleIsMutable(role);

        long linkedUsers = userRepository.countByRoles_Id(id);
        if (linkedUsers > 0) {
            throw new IllegalArgumentException("Cannot delete a role assigned to users");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> listAvailablePermissions() {
        return permissionService.getAllPermissionDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDto> listRolePermissions(Long roleId, int page, int size, String search, String category, String sortBy, String sortDirection) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ObjectNotFoundException(Role.class.getSimpleName(), roleId));

        String normalizedSearch = search != null ? search.trim().toLowerCase() : "";
        String normalizedCategory = category != null ? category.trim().toLowerCase() : "";
        boolean ascending = "asc".equalsIgnoreCase(sortDirection);

        List<PermissionDto> filtered = role.getPermissions().stream()
                .map(permissionService::resolve)
                .filter(permission -> permission != null)
                .filter(permission -> normalizedCategory.isBlank() || permission.getCategory().equalsIgnoreCase(normalizedCategory))
                .filter(permission -> normalizedSearch.isBlank()
                        || lower(permission.getCode()).contains(normalizedSearch)
                        || lower(permission.getName()).contains(normalizedSearch)
                        || lower(permission.getDescription()).contains(normalizedSearch))
                .sorted(buildPermissionComparator(sortBy, ascending))
                .toList();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        int fromIndex = (int) pageable.getOffset();
        if (fromIndex >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }

        int toIndex = Math.min(fromIndex + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(fromIndex, toIndex), pageable, filtered.size());
    }

    private String normalizeName(String rawName) {
        if (!StringUtils.hasText(rawName)) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        return rawName.trim();
    }

    private Set<String> sanitizeAndValidatePermissions(List<String> permissionCodeList) {
        if (permissionCodeList == null) {
            return new HashSet<>();
        }

        Set<String> uniqueCodes = permissionCodeList.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> invalidCodes = uniqueCodes.stream()
                .filter(code -> !permissionService.isValidPermission(code))
                .sorted()
                .toList();

        if (!invalidCodes.isEmpty()) {
            throw new IllegalArgumentException("Invalid permission codes: " + String.join(", ", invalidCodes));
        }

        return uniqueCodes;
    }

    private RoleDto mapToDto(Role role) {
        RoleType effectiveType = role.getType() != null ? role.getType() : RoleType.CUSTOM;
        Boolean active = role.getActive() != null ? role.getActive() : true;
        return new RoleDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                effectiveType,
                active,
                role.getPermissions().stream().sorted().toList(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private void assertRoleIsMutable(Role role) {
        if (RoleType.SYSTEM.equals(role.getType()) || ADMIN_ROLE_NAME.equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("System roles cannot be modified via API");
        }
    }

    private Comparator<PermissionDto> buildPermissionComparator(String sortBy, boolean ascending) {
        Comparator<PermissionDto> comparator = switch (sortBy == null ? "code" : sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(permission -> lower(permission.getName()));
            case "category", "resource" -> Comparator.comparing(permission -> lower(permission.getCategory()));
            case "description" -> Comparator.comparing(permission -> lower(permission.getDescription()));
            default -> Comparator.comparing(permission -> lower(permission.getCode()));
        };
        return ascending ? comparator : comparator.reversed();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
