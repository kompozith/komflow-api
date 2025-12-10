package com.kompozith.komflow.features.personnel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDetailsDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String language;

    private List<PhoneNumberDto> phoneNumbers; // Embed PhoneNumberDto

    private Instant createdAt;
    private Instant updatedAt;
}
