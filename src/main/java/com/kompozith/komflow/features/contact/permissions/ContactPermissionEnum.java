package com.kompozith.komflow.features.contact.permissions;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ContactPermissionEnum {

    // Tag
    TAG_LIST("TAG_LIST", "Tags list", "List all tags"),
    TAG_CREATE("TAG_CREATE", "Tag create", "Create a tag"),
    TAG_SHOW("TAG_SHOW", "Tag show", "Show a tag details"),
    TAG_UPDATE("TAG_UPDATE", "Tag edit", "Edit a tag"),
    TAG_DELETE("TAG_DELETE", "Tag delete", "Delete a tag"),

    // Contact
    CONTACT_LIST("CONTACT_LIST", "Contact list", "List all contacts"),
    CONTACT_SHOW("CONTACT_SHOW", "Contact details", "Show a contact details"),
    CONTACT_CREATE("CONTACT_CREATE", "Contact create", "Create a contact"),
    CONTACT_UPDATE("CONTACT_UPDATE", "Contact edit", "Edit a contact"),
    CONTACT_DELETE("CONTACT_DELETE", "Contact delete", "Delete a contact");

    private final String code;

    private final String name;

    private final String description;

    ContactPermissionEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static List<String> getAllCodes() {
        return Arrays.stream(values()).map(ContactPermissionEnum::getCode).toList();
    }
}