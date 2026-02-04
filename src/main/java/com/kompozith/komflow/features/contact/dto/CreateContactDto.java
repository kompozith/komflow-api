package com.kompozith.komflow.features.contact.dto;

import com.kompozith.komflow.features.contact.validation.ValidPersonSelection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;
import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidPersonSelection
public class CreateContactDto {

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    // Use existing person
    private Long personId;

    // Or create a new person
    private CreatePersonDto person;

    // Optional phone numbers when creating a new person
    private List<CreatePhoneNumberDto> phoneNumbers;

    private List<Long> tagIds; // List of tag IDs
}
