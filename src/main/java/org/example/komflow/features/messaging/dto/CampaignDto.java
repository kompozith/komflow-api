package org.example.komflow.features.messaging.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.komflow.features.contact.dto.ContactDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {

    private Long id;

    @NotBlank(message = "campaign.name.notBlank")
    @Size(max = 255, message = "campaign.name.sizeExceeded") // Assuming max=255 is the only size constraint
    private String name;

    private String description;

    @NotNull(message = "campaign.messageId.notNull")
    private Long messageId; // Representing the Message relationship

    private List<ContactDto> contacts;

    private List<ContactDto> mailCc;

    private List<ContactDto> mailCci;

    private Instant createdAt;

    private Instant updatedAt;
}
