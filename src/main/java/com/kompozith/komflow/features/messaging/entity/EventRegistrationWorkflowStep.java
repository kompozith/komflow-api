package com.kompozith.komflow.features.messaging.entity;

import com.kompozith.komflow.features.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "msg_event_registration_workflow_steps")
public class EventRegistrationWorkflowStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "msg_event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "msg_message_id")
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 30)
    private EventWorkflowStepType stepType = EventWorkflowStepType.SEND_MESSAGE;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 30)
    private EventWorkflowRecipientType recipientType = EventWorkflowRecipientType.REGISTRANT;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 40)
    private EventWorkflowConditionType conditionType;

    @Column(name = "condition_value", length = 255)
    private String conditionValue;

    @Column(nullable = false)
    private Integer position = 1;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "recipient_emails", columnDefinition = "TEXT")
    private String recipientEmails;
}
