package com.kompozith.komflow.features.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventRegistrationRequestDto {

    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean whatsappNumber;

    private String language;
    private String country;
    private String city;
    private String timezone;

    private String civility;
    private String profession;
    private String ageRange;
    private String objectives;
    private String websiteUrl;
}

