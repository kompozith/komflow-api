package com.kompozith.komflow.features.personnel.dto;

import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import lombok.Builder;
import lombok.Data;

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
