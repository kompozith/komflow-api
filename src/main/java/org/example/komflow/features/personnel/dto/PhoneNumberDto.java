package org.example.komflow.features.personnel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberDto {

    private Long id;

    @NotBlank(message = "phoneNumber.number.notBlank")
    @Size(max = 50, message = "phoneNumber.number.sizeExceeded")
    private String number;

    @Size(max = 10, message = "phoneNumber.isWhatsapp.sizeExceeded")
    private String isWhatsapp;

    private Long personId; // ID of the associated Person
    private Long contactId; // ID of the associated Contact

    private Instant createdAt;
    private Instant updatedAt;
}
