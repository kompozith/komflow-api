package com.kompozith.komflow.features.contact.dto;

import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactWithTagCountDto {

    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    private PersonDto person;

    private Long tagCount;

    private Instant createdAt;

    private Instant updatedAt;

    public ContactWithTagCountDto(Long id, Boolean enabled, Instant lastMessageReceivedAt, Instant createdAt, Instant updatedAt, Long tagCount, Long personId, String email, String firstName, String lastName, String language, Instant personCreatedAt, Instant personUpdatedAt, String phoneNumber) {
        this.id = id;
        this.enabled = enabled;
        this.lastMessageReceivedAt = lastMessageReceivedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tagCount = tagCount;
        this.person = new PersonDto(
            personId,
            email,
            firstName,
            lastName,
            language,
            phoneNumber,
            personCreatedAt,
            personUpdatedAt
        );
    }
}