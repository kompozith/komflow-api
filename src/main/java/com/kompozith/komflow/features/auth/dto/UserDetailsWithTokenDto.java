package com.kompozith.komflow.features.auth.dto;

import com.kompozith.komflow.features.personnel.dto.PhoneNumberListDto;
import com.kompozith.komflow.features.personnel.dto.UserDetailsDto;
import com.kompozith.komflow.features.personnel.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.stream.Collectors;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailsWithTokenDto extends UserDetailsDto {

    private String token;

    public static UserDetailsWithTokenDto mapToUserDetailsWithTokenDto(User user, String token) {
        return UserDetailsWithTokenDto.builder()
                .username(user.getUsername())
                .email(user.getPerson().getEmail())
                .firstName(user.getPerson().getFirstName())
                .lastName(user.getPerson().getLastName())
                .phoneNumbers((user.getPerson().getPhoneNumbers().stream().map(
                        PhoneNumberListDto::mapToPhoneNumberDto
                )).collect(Collectors.toList()))
                .token(token)
                .build();
    }
}
