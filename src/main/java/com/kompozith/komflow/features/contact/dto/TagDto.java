package com.kompozith.komflow.features.contact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class TagDto {
    private Long id;

    @Schema(description = "Tag name", example = "Tag test")
    @NotBlank(message = "tag.name.blank")
    private String name;

    @Schema(description = "Tag description", example = "Web app service customers")
    private String description;

    @Schema(description = "Tag color code", example = "#FFF6754B")
    @NotBlank(message = "tag.colorCode.blank")
    private String colorCode;

    @Schema(description = "Number of contacts associated with this tag", example = "5")
    private Long contactCount;

    @Schema(description = "Whether the tag is enabled", example = "true")
    private Boolean enabled;

    private Instant createdAt;
    private Instant updatedAt;
}
