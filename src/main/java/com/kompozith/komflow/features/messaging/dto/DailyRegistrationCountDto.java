package com.kompozith.komflow.features.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyRegistrationCountDto {
    private String date;  // "yyyy-MM-dd"
    private long count;
}
