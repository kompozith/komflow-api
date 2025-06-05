package org.example.komflow.features.contact.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class TagDto {
    private Long id;

    @NotBlank(message = "tag.name.notBlank")
    private String name;

    private String description;

    @NotBlank(message = "tag.colorCode.notBlank")
    private String colorCode;

    private Instant createdAt;
    private Instant updatedAt;
}
