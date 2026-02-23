package com.kompozith.komflow.features.auth.controller;

import com.kompozith.komflow.features.auth.dto.PermissionDto;
import com.kompozith.komflow.features.auth.dto.RoleDto;
import com.kompozith.komflow.features.auth.dto.RolePermissionsUpdateDto;
import com.kompozith.komflow.features.auth.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@Tag(name = "Role Management", description = "Role and permission management APIs")
public class RoleController {

    private final RoleManagementService roleManagementService;

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping
    @Operation(summary = "List roles", description = "Get paginated roles with optional search and sorting")
    public ResponseEntity<Page<RoleDto>> listRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(roleManagementService.listRoles(page, size, search, sortBy, sortDirection));
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/permissions")
    @Operation(summary = "List available permissions", description = "Get all permission codes and labels available in the system")
    public ResponseEntity<List<PermissionDto>> listPermissions() {
        return ResponseEntity.ok(roleManagementService.listAvailablePermissions());
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get role details", description = "Get one role by id with assigned permissions")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleManagementService.getRoleById(id));
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PostMapping
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleDto created = roleManagementService.createRole(roleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update role metadata and optionally permissions")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(roleManagementService.updateRole(id, roleDto));
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update role permissions", description = "Replace all permissions assigned to a role")
    public ResponseEntity<RoleDto> updateRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionsUpdateDto updateDto
    ) {
        return ResponseEntity.ok(roleManagementService.updateRolePermissions(id, updateDto.getPermissionCodeList()));
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/{id}/permissions")
    @Operation(summary = "List role permissions", description = "Get paginated permissions assigned to a role")
    public ResponseEntity<Page<PermissionDto>> listRolePermissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(roleManagementService.listRolePermissions(id, page, size, search, category, sortBy, sortDirection));
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role that is not assigned to users")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleManagementService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
