package com.komflow.kompozith.features.personnel.dto;

import com.komflow.kompozith.features.personnel.entity.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
public class PhoneNumberListDto {

    private Long id;
    private String number;
    private String isWhatsapp;

    public static PhoneNumberListDto mapToPhoneNumberDto(PhoneNumber phoneNumber) {
        return PhoneNumberListDto.builder()
                .id(phoneNumber.getId())
                .number(phoneNumber.getNumber())
                .isWhatsapp(phoneNumber.getIsWhatsapp())
                .build();
    };
}
