package com.kompozith.komflow.features.contact.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContactDto {

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    @NotNull(message = "contact.personId.notNull")
    private Long personId; // Representing the Person relationship

    private List<Long> tagIds; // List of tag IDs
}