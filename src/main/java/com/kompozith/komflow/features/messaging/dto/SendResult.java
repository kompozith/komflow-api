package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {
    private int totalContacts;
    private int successfulSends;
    private int failedSends;
    private List<String> sentIdentifiers; // emails, phone numbers, etc.
    private List<String> failedIdentifiers; // emails, phone numbers, etc.
}