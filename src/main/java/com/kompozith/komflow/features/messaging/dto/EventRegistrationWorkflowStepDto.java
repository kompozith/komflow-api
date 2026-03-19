package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.EventWorkflowConditionType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowRecipientType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowStepType;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationWorkflowStepDto {
    private Long id;
    private Long messageId;
    private String messageTitle;
    private MessageChannel messageChannel;
    private EventWorkflowStepType stepType;
    private EventWorkflowRecipientType recipientType;
    private Integer delayMinutes;
    private EventWorkflowConditionType conditionType;
    private String conditionValue;
    private Integer position;
    private boolean enabled;
    private String recipientEmails;
}
