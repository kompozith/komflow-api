package org.example.komflow.features.contact.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TagDto {
    private String id;
    private String name;
    private String description;
    private String colorCode;
    private Instant createdAt;
    private Instant updatedAt;
}
