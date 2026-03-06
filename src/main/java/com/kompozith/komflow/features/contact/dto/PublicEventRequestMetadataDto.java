package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventRequestMetadataDto {
    private String language;
    private String timezone;
    private String country;
    private String city;
    private String clientIp;
    private String userAgent;
}
