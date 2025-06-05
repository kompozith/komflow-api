package org.example.komflow.features.personnel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

    private Long id;

    @Email(message = "person.email.invalidFormat")
    @Size(max = 255, message = "person.email.sizeExceeded")
    private String email;

    @NotBlank(message = "person.firstName.notBlank")
    @Size(max = 100, message = "person.firstName.sizeExceeded")
    private String firstName;

    @NotBlank(message = "person.lastName.notBlank")
    @Size(max = 100, message = "person.lastName.sizeExceeded")
    private String lastName;

    @Size(max = 50, message = "person.language.sizeExceeded")
    private String language;

    private List<PhoneNumberDto> phoneNumbers; // Embed PhoneNumberDto

    private Instant createdAt;
    private Instant updatedAt;
}
