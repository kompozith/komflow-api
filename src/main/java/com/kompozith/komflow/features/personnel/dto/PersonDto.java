package com.kompozith.komflow.features.personnel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String language;
    private String country;
    private String city;
    private String timezone;

    private String phoneNumber; // Embed PhoneNumberDto

    private Instant createdAt;
    private Instant updatedAt;
}
