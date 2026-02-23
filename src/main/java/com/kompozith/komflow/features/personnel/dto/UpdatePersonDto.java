package com.kompozith.komflow.features.personnel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonDto {

    @NotBlank(message = "person.email.blank")
    @Email(message = "person.email.format")
    private String email;

    private String firstName;

    private String lastName;

    private String language;
    private String country;
    private String city;
    private String timezone;
}
