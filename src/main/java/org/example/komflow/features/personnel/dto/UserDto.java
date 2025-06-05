package org.example.komflow.features.personnel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "user.username.notBlank")
    @Size(min = 3, max = 100, message = "user.username.size")
    private String username;

    // Password is intentionally omitted for security reasons in response DTOs.
    // For creation/update, specific DTOs or fields should be used.

    private boolean enabled;

    @NotNull(message = "user.person.notNull")
    private PersonDto person; // Embed PersonDto

    private List<Long> roleIds; // Placeholder for Role DTOs

    // Audit logs (logs field) are omitted for brevity.

    private Instant createdAt;
    private Instant updatedAt;
}
