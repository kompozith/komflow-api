package com.kompozith.komflow.features.contact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class TagDto {
    protected Long id;

    @Schema(description = "Tag name", example = "Tag test")
    @NotBlank(message = "tag.name.blank")
    protected String name;

    @Schema(description = "Tag description", example = "Web app service customers")
    protected String description;

    @Schema(description = "Tag color code", example = "#FFF6754B")
    @NotBlank(message = "tag.colorCode.blank")
    protected String colorCode;

    @Schema(description = "Number of contacts associated with this tag", example = "5")
    protected Long contactCount;

    @Schema(description = "Whether the tag is enabled", example = "true")
    protected Boolean enabled;

    @Schema(description = "IDs of contacts linked to this tag")
    protected List<Long> contactIds;

    protected Instant createdAt;
    protected Instant updatedAt;
}
