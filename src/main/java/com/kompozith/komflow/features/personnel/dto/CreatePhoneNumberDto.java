package com.kompozith.komflow.features.personnel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePhoneNumberDto {

    @NotBlank(message = "Phone number is required")
    private String number;

    private Boolean isWhatsapp = false;
}