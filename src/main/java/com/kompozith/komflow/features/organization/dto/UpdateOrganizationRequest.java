package com.kompozith.komflow.features.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(

    @NotBlank(message = "organization.name.blank")
    @Size(min = 2, max = 255)
    String name,

    @Pattern(regexp = "^[a-z0-9-]*$", message = "organization.slug.format")
    @Size(max = 100)
    String slug
) {}
