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
}
