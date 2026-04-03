package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageTestDirectRequestDto {
    /**
     * Email address (for EMAIL channel) or phone number (for SMS / WHATSAPP channel).
     */
    private String recipient;
}
