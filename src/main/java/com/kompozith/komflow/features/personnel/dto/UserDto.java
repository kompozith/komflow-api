package com.kompozith.komflow.features.personnel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    // Password is intentionally omitted for security reasons in response DTOs.
    // For creation/update, specific DTOs or fields should be used.

    private boolean enabled;

    @NotNull(message = "user.person.notNull")
    private PersonDto person; // Embed PersonDto

    private List<Long> roleIds; // Placeholder for Role DTOs

    private Instant createdAt;
    private Instant updatedAt;
}
