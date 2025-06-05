package org.example.komflow.features.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private Long id;

    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private List<PermissionDto> permissions; // Embed PermissionDto

    private Instant createdAt;
    private Instant updatedAt;
}
