package com.kompozith.komflow.features.personnel.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PersonnelPermissionEnum {

    // Personnel
    PERSONNEL_VIEW("PERSONNEL_VIEW", "Personnel view", "View personnel information"),
    PERSONNEL_MANAGE("PERSONNEL_MANAGE", "Personnel manage", "Manage personnel information (add/edit/delete phone numbers)");

    private final String code;

    private final String name;

    private final String description;

    PersonnelPermissionEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static List<String> getAllCodes() {
        return Arrays.stream(values()).map(PersonnelPermissionEnum::getCode).toList();
    }
}