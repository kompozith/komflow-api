package com.kompozith.komflow.features.core.util;

import com.kompozith.komflow.features.auth.dto.PermissionDto;
import com.kompozith.komflow.features.contact.permissions.ContactPermissionEnum;
import com.kompozith.komflow.features.messaging.permissions.MessagePermissionEnum;
import com.kompozith.komflow.features.personnel.permissions.PersonnelPermissionEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final Map<String, PermissionDto> permissionMap;

    public PermissionService() {
        this.permissionMap = new HashMap<>();
        Arrays.stream(ContactPermissionEnum.values()).forEach(permission -> permissionMap.put(
                permission.getCode(),
                new PermissionDto(permission.getCode(), permission.getName(), permission.getDescription(), "CONTACT")
        ));
        Arrays.stream(MessagePermissionEnum.values()).forEach(permission -> permissionMap.put(
                permission.getCode(),
                new PermissionDto(permission.getCode(), permission.getName(), permission.getDescription(), "MESSAGE")
        ));
        Arrays.stream(PersonnelPermissionEnum.values()).forEach(permission -> permissionMap.put(
                permission.getCode(),
                new PermissionDto(permission.getCode(), permission.getName(), permission.getDescription(), "PERSONNEL")
        ));
    }

    public boolean isValidPermission(String permissionCode) {
        return permissionMap.containsKey(permissionCode);
    }

    public List<String> getAllValidPermissions() {
        return new ArrayList<>(permissionMap.keySet());
    }

    public PermissionDto resolve(String permissionCode) {
        return permissionMap.get(permissionCode);
    }

    public List<PermissionDto> getAllPermissionDetails() {
        return permissionMap.values().stream()
                .sorted((left, right) -> left.getCode().compareToIgnoreCase(right.getCode()))
                .collect(Collectors.toList());
    }
}
