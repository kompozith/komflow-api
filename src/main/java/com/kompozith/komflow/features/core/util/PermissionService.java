package com.kompozith.komflow.features.core.util;

import com.kompozith.komflow.features.contact.permissions.ContactPermissionEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final Map<String, ContactPermissionEnum> permissionMap;

    public PermissionService() {
        this.permissionMap = Arrays.stream(ContactPermissionEnum.values())
            .collect(Collectors.toMap(
                ContactPermissionEnum::getCode,
                Function.identity()
            ));
    }

    public boolean isValidPermission(String permissionCode) {
        return permissionMap.containsKey(permissionCode);
    }

    public List<String> getAllValidPermissions() {
        return new ArrayList<>(permissionMap.keySet());
    }

    public ContactPermissionEnum resolve(String permissionCode) {
        return permissionMap.get(permissionCode);
    }
}