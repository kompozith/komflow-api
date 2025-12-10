package com.kompozith.komflow.features.contact.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TagWithContactCountDto extends TagDto {
    private Long contactCount;

    public TagWithContactCountDto(Long id, String name, String description, String colorCode, Boolean enabled, Instant createdAt, Instant updatedAt, Long contactCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.contactCount = contactCount;
    }

    public TagWithContactCountDto() {

    }
}