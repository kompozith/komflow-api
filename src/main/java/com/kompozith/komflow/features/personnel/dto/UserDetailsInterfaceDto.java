package com.kompozith.komflow.features.personnel.dto;

import java.time.Instant;

public interface UserDetailsInterfaceDto {

    String getId();
    String getUsername();
    String getEmail();
    String getFirstName();
    String getLastName();
    Instant getCreatedAt();
}
