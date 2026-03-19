package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.EventWorkflowConditionType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowRecipientType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowStepType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRegistrationWorkflowStepDto {
    private Long messageId;
    private EventWorkflowStepType stepType;
    private EventWorkflowRecipientType recipientType;
    private Integer delayMinutes;
    private EventWorkflowConditionType conditionType;
    private String conditionValue;
    private Integer position;
    private Boolean enabled;
    private String recipientEmails;
}
