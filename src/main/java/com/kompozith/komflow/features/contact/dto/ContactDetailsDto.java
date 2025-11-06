package com.kompozith.komflow.features.contact.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.kompozith.komflow.features.personnel.dto.PersonDto;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetailsDto {

    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    private PersonDto person;

    private List<TagDto> tags;

    private Instant createdAt;

    private Instant updatedAt;
}