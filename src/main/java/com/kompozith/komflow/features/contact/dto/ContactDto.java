package com.kompozith.komflow.features.contact.dto;

import com.kompozith.komflow.features.personnel.dto.PersonDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
// Assuming TagDto is in the same package, otherwise add import


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {

    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    private PersonDto person;

    private Integer tagCount;

    private Instant createdAt;

    private Instant updatedAt;
}
