package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventScheduleDto {
    private String timezoneLabel;
    private boolean rangeSameDay;
    private String singleDateTime;
    private String sameDayDate;
    private String sameDayTimeRange;
    private String startDateTime;
    private String endDateTime;
}
