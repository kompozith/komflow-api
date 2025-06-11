package com.komflow.kompozith.features.contact.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class TagDto {
    private Long id;

    @NotBlank(message = "tag.name.blank")
    private String name;

    private String description;

    @NotBlank(message = "tag.colorCode.blank")
    private String colorCode;

    private Instant createdAt;
    private Instant updatedAt;
}
