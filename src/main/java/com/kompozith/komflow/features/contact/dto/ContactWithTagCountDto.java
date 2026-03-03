package com.kompozith.komflow.features.contact.dto;

import com.kompozith.komflow.features.personnel.dto.PersonDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactWithTagCountDto {

    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    private String civility;

    private String profession;

    private String ageRange;

    private String objectives;

    private String websiteUrl;

    private PersonDto person;

    private Long tagCount;

    private Instant createdAt;

    private Instant updatedAt;

    public ContactWithTagCountDto(
            Long id,
            Boolean enabled,
            Instant lastMessageReceivedAt,
            String civility,
            String profession,
            String ageRange,
            String objectives,
            String websiteUrl,
            Instant createdAt,
            Instant updatedAt,
            Long tagCount,
            Long personId,
            String email,
            String firstName,
            String lastName,
            String language,
            String country,
            String city,
            String timezone,
            Instant personCreatedAt,
            Instant personUpdatedAt,
            String phoneNumber
    ) {
        this.id = id;
        this.enabled = enabled;
        this.lastMessageReceivedAt = lastMessageReceivedAt;
        this.civility = civility;
        this.profession = profession;
        this.ageRange = ageRange;
        this.objectives = objectives;
        this.websiteUrl = websiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tagCount = tagCount;
        this.person = new PersonDto(
            personId,
            email,
            firstName,
            lastName,
            language,
            country,
            city,
            timezone,
            phoneNumber,
            personCreatedAt,
            personUpdatedAt
        );
    }
}
