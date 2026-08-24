package com.kompozith.komflow.features.personnel.dto;

import com.kompozith.komflow.features.personnel.entity.User;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@SuperBuilder
public class UserDetailsDto {

    private String email;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private List<PhoneNumberListDto> phoneNumbers;

    public static UserDetailsDto mapFromUser(User user) {
        return UserDetailsDto.builder()
                .email(user.getPerson().getEmail())
                .firstName(user.getPerson().getFirstName())
                .lastName(user.getPerson().getLastName())
                .createdAt(user.getCreatedAt())
                .phoneNumbers(!Objects.isNull(user.getPerson().getPhoneNumbers()) ?
                    (user.getPerson().getPhoneNumbers().stream().map( PhoneNumberListDto::mapToPhoneNumberDto)
                ).collect(Collectors.toList()) : new ArrayList<PhoneNumberListDto>())
                .build();
    }

    public static UserDetailsDto mapFormUserDetailsInterface(UserDetailsInterfaceDto userDetailsInterfaceDto) {
        return UserDetailsDto.builder()
                .email(userDetailsInterfaceDto.getEmail())
                .firstName(userDetailsInterfaceDto.getFirstName())
                .lastName(userDetailsInterfaceDto.getLastName())
                .createdAt(userDetailsInterfaceDto.getCreatedAt())
                .build();
    }
}
