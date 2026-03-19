package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.entity.EventRegistrationWorkflowStep;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowConditionType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowRecipientType;
import com.kompozith.komflow.features.messaging.entity.EventWorkflowStepType;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationWorkflowExecutor {

    private final EventRepository eventRepository;
    private final MessageDispatcherService messageDispatcherService;
    private final TaskScheduler taskScheduler;

    @Value("${app.notifications.admin-emails:}")
    private String adminEmails;

    public void execute(Long eventId, Contact registrant) {
        if (eventId == null || registrant == null) {
            return;
        }

        Optional<Event> eventOptional = eventRepository.findByIdWithWorkflowSteps(eventId);
        if (eventOptional.isEmpty()) {
            return;
        }

        Event event = eventOptional.get();
        List<EventRegistrationWorkflowStep> steps = event.getRegistrationWorkflowSteps();
        if (steps == null || steps.isEmpty()) {
            return;
        }

        executeSteps(event, registrant, steps, 0);
    }

    private void executeSteps(Event event, Contact registrant, List<EventRegistrationWorkflowStep> steps, int startIndex) {
        if (steps == null || steps.isEmpty() || startIndex >= steps.size()) {
            return;
        }

        Instant eventInstant = event.getStartAt();
        for (int i = startIndex; i < steps.size(); i++) {
            EventRegistrationWorkflowStep step = steps.get(i);
            if (step == null || !step.isEnabled()) {
                continue;
            }

            EventWorkflowStepType stepType = step.getStepType() != null
                    ? step.getStepType()
                    : EventWorkflowStepType.SEND_MESSAGE;

            if (stepType == EventWorkflowStepType.CONDITION) {
                if (!evaluateCondition(step.getConditionType(), registrant)) {
                    return;
                }
                continue;
            }

            if (stepType == EventWorkflowStepType.DELAY) {
                Integer delayMinutes = step.getDelayMinutes();
                if (delayMinutes == null || delayMinutes <= 0) {
                    continue;
                }
                int nextIndex = i + 1;
                taskScheduler.schedule(
                        () -> executeSteps(event, registrant, steps, nextIndex),
                        Instant.now().plus(Duration.ofMinutes(delayMinutes))
                );
                return;
            }

            Message message = step.getMessage();
            if (message == null) {
                continue;
            }

            MessageChannel channel = message.getChannel();
            EventWorkflowRecipientType recipientType = step.getRecipientType() != null
                    ? step.getRecipientType()
                    : EventWorkflowRecipientType.REGISTRANT;
            if (recipientType == EventWorkflowRecipientType.ADMIN) {
                List<Contact> admins = buildAdminRecipients(step.getRecipientEmails());
                for (Contact admin : admins) {
                    dispatchIfPossible(admin, message, channel, eventInstant);
                }
            } else {
                dispatchIfPossible(registrant, message, channel, eventInstant);
            }
        }
    }

    private boolean evaluateCondition(EventWorkflowConditionType conditionType, Contact registrant) {
        if (conditionType == null || registrant == null || registrant.getPerson() == null) {
            return true;
        }

        return switch (conditionType) {
            case CONTACT_HAS_EMAIL -> StringUtils.hasText(registrant.getPerson().getEmail());
            case CONTACT_HAS_PHONE -> registrant.getPerson().getPhoneNumbers() != null
                    && registrant.getPerson().getPhoneNumbers().stream()
                    .anyMatch(phone -> phone != null && StringUtils.hasText(phone.getNumber()));
        };
    }

    private void dispatchIfPossible(Contact contact, Message message, MessageChannel channel, Instant eventInstant) {
        if (!messageDispatcherService.canSendToContact(contact, channel)) {
            log.warn("Skipping workflow message {} for contact {} via {}", message.getId(), contact.getId(), channel);
            return;
        }
        messageDispatcherService.sendToContact(contact, message, channel, eventInstant);
    }

    private List<Contact> buildAdminRecipients(String stepEmails) {
        Set<String> emails = new LinkedHashSet<>();
        if (StringUtils.hasText(stepEmails)) {
            emails.addAll(splitEmails(stepEmails));
        }
        if (StringUtils.hasText(adminEmails)) {
            emails.addAll(splitEmails(adminEmails));
        }

        List<Contact> contacts = new ArrayList<>();
        for (String email : emails) {
            if (!StringUtils.hasText(email)) {
                continue;
            }
            Contact admin = new Contact();
            var person = new com.kompozith.komflow.features.personnel.entity.Person();
            person.setEmail(email.trim().toLowerCase(Locale.ROOT));
            admin.setPerson(person);
            contacts.add(admin);
        }
        return contacts;
    }

    private List<String> splitEmails(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
