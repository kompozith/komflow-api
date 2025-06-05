package org.example.komflow.features.contact.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
// Assuming TagDto is in the same package, otherwise add import

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {

    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    @NotNull(message = "contact.personId.notNull")
    private Long personId; // Representing the Person relationship

    private List<TagDto> tags; // Using existing TagDto

    private Instant createdAt;

    private Instant updatedAt;
}
