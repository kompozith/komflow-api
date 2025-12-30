package com.kompozith.komflow.features.messaging.permissions;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum MessagePermissionEnum {

    // Message
    MESSAGE_LIST("MESSAGE_LIST", "Message list", "List all messages"),
    MESSAGE_SHOW("MESSAGE_SHOW", "Message details", "Show a message details"),
    MESSAGE_CREATE("MESSAGE_CREATE", "Message create", "Create a message"),
    MESSAGE_UPDATE("MESSAGE_UPDATE", "Message edit", "Edit a message"),
    MESSAGE_DELETE("MESSAGE_DELETE", "Message delete", "Delete a message"),
    MESSAGE_SEND_TO_CONTACT("MESSAGE_SEND_TO_CONTACT", "Message send to contact", "Send a message to a specific contact"),
    MESSAGE_SEND_TO_TAG("MESSAGE_SEND_TO_TAG", "Message send to tag", "Send a message to all contacts in a tag"),

    // Campaign
    CAMPAIGN_LIST("CAMPAIGN_LIST", "Campaign list", "List all campaigns"),
    CAMPAIGN_SHOW("CAMPAIGN_SHOW", "Campaign details", "Show a campaign details"),
    CAMPAIGN_CREATE("CAMPAIGN_CREATE", "Campaign create", "Create a campaign"),
    CAMPAIGN_UPDATE("CAMPAIGN_UPDATE", "Campaign edit", "Edit a campaign"),
    CAMPAIGN_DELETE("CAMPAIGN_DELETE", "Campaign delete", "Delete a campaign"),
    CAMPAIGN_SUBMIT("CAMPAIGN_SUBMIT", "Campaign submit", "Submit a campaign");

    private final String code;

    private final String name;

    private final String description;

    MessagePermissionEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static List<String> getAllCodes() {
        return Arrays.stream(values()).map(MessagePermissionEnum::getCode).toList();
    }
}