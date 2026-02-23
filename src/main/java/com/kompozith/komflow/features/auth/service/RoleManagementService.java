package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.dto.PermissionDto;
import com.kompozith.komflow.features.auth.dto.RoleDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleManagementService {
    Page<RoleDto> listRoles(int page, int size, String search, String sortBy, String sortDirection);

    RoleDto getRoleById(Long id);

    RoleDto createRole(RoleDto roleDto);

    RoleDto updateRole(Long id, RoleDto roleDto);

    RoleDto updateRolePermissions(Long id, List<String> permissionCodeList);

    void deleteRole(Long id);

    List<PermissionDto> listAvailablePermissions();

    Page<PermissionDto> listRolePermissions(Long roleId, int page, int size, String search, String category, String sortBy, String sortDirection);
}
