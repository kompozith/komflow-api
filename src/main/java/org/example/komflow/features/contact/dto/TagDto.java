package org.example.komflow.features.contact.dto;

import jakarta.validation.constraints.NotBlank; // Import added
import lombok.Data;

import java.time.Instant;

@Data
public class TagDto {
    private Long id; // Changed from String to Long

    @NotBlank // Added
    private String name;

    private String description;

    @NotBlank // Added
    private String colorCode;

    private Instant createdAt;
    private Instant updatedAt;
}
