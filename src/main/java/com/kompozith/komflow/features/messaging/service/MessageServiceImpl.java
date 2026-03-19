package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.repository.FileRepository;
import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.dto.SendResult;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.mapper.MessageMapper;
import com.kompozith.komflow.features.messaging.repository.EventRepository;
import com.kompozith.komflow.features.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private static final Pattern EVENT_VARIABLE_PATTERN = Pattern.compile("\\{\\{event[^}]+\\}\\}", Pattern.CASE_INSENSITIVE);
    private static final String LEGACY_EVENT_START_AT = "{{eventStartAt}}";
    private static final String LEGACY_EVENT_END_AT = "{{eventEndAt}}";
    private static final String EVENT_START_DATE = "{{eventStartDate}}";
    private static final String EVENT_START_TIME = "{{eventStartTime}}";
    private static final String EVENT_END_DATE = "{{eventEndDate}}";
    private static final String EVENT_END_TIME = "{{eventEndTime}}";

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ContactRepository contactRepository;
    private final TagRepository tagRepository;
    private final FileRepository fileRepository;
    private final EventRepository eventRepository;
    private final MessageDispatcherService messageDispatcherService;
    private final MessageContentParserService messageContentParserService;
    private final EmailService emailService; // Keep for backward compatibility

    @Override
    @Transactional
    public MessageDto create(CreateMessageDto createMessageDto) {
        validateCreateMessage(createMessageDto);
        createMessageDto.setContent(normalizeLegacyEventVariables(createMessageDto.getContent()));
        createMessageDto.setContent(messageContentParserService.normalizeForStorage(createMessageDto.getContent(), createMessageDto.getChannel()));
        Message message = messageMapper.createMessageDtoToMessage(createMessageDto);
        message.setAttachments(resolveAttachmentEntities(createMessageDto.getAttachments()));
        message.setEvent(resolveEventForMessage(createMessageDto.getEventId()));
        Message savedMessage = messageRepository.save(message);

        log.info("Message created with id: {}", savedMessage.getId());
        return messageMapper.messageToMessageDto(savedMessage);
    }

    @Override
    public List<MessageDto> findAll() {
        return messageRepository.findAll().stream()
                .map(messageMapper::messageToMessageDto)
                .map(this::normalizeMessageDtoContent)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageDto> findAll(Pageable pageable) {
        return messageRepository.findAll(pageable)
                .map(messageMapper::messageToMessageDto)
                .map(this::normalizeMessageDtoContent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> findAll(Pageable pageable, MessageChannel channel, String search, Instant createdAtFrom, Instant createdAtTo) {
        Pageable sanitizedPageable = pageable == null
                ? Pageable.unpaged()
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());

        return messageRepository.findWithFilters(
                channel != null ? channel.name() : null,
                search,
                createdAtFrom,
                createdAtTo,
                sanitizedPageable
        ).map(messageMapper::messageToMessageDto)
         .map(this::normalizeMessageDtoContent);
    }

    @Override
    public MessageDto findById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), id));
        return normalizeMessageDtoContent(messageMapper.messageToMessageDto(message));
    }

    @Override
    @Transactional
    public MessageDto update(Long id, CreateMessageDto createMessageDto) {
        validateCreateMessage(createMessageDto);
        createMessageDto.setContent(normalizeLegacyEventVariables(createMessageDto.getContent()));
        createMessageDto.setContent(messageContentParserService.normalizeForStorage(createMessageDto.getContent(), createMessageDto.getChannel()));
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), id));

        message.setTitle(createMessageDto.getTitle());
        message.setContent(createMessageDto.getContent());
        message.setChannel(createMessageDto.getChannel());
        message.setAttachments(resolveAttachmentEntities(createMessageDto.getAttachments()));
        message.setEvent(resolveEventForMessage(createMessageDto.getEventId()));

        Message updatedMessage = messageRepository.save(message);

        log.info("Message updated with id: {}", id);
        return normalizeMessageDtoContent(messageMapper.messageToMessageDto(updatedMessage));
    }

    @Override
    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ObjectNotFoundException(Message.class.getSimpleName(), id);
        }

        messageRepository.deleteById(id);
        log.info("Message deleted with id: {}", id);
    }

    @Override
    public Message findEntityById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), id));
    }

    @Override
    public SendResult sendToTag(Long tagId, Long messageId, MessageChannel channel) {
        // Validate parameters
        if (tagId == null || tagId <= 0) {
            throw new IllegalArgumentException("Invalid tag ID");
        }
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Invalid message ID");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel is required");
        }

        // Load tag with contacts
        Tag tag = tagRepository.findByIdWithContacts(tagId)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), "id", tagId.toString()));

        if (tag.getContacts() == null || tag.getContacts().isEmpty()) {
            throw new IllegalArgumentException("Tag has no associated contacts");
        }

        // Load message
        Message message = findEntityById(messageId);

        // Send messages to all contacts (using HashSet to ensure uniqueness)
        Set<Contact> uniqueContacts = new HashSet<>(tag.getContacts());
        int successCount = 0;
        int failureCount = 0;
        List<String> sentIdentifiers = new ArrayList<>();
        List<String> failedIdentifiers = new ArrayList<>();

        for (Contact contact : uniqueContacts) {
            try {
                if (messageDispatcherService.canSendToContact(contact, channel)) {
                    messageDispatcherService.sendToContact(contact, message, channel);
                    successCount++;
                    String identifier = messageDispatcherService.getRecipientIdentifier(contact, channel);
                    sentIdentifiers.add(identifier != null ? identifier : "Contact " + contact.getId());
                } else {
                    failureCount++;
                    failedIdentifiers.add("Contact " + contact.getId() + " (no valid " + channel.toString().toLowerCase() + " info)");
                    log.warn("Contact {} has no valid {} information", contact.getId(), channel);
                }
            } catch (Exception e) {
                failureCount++;
                String identifier = messageDispatcherService.getRecipientIdentifier(contact, channel);
                failedIdentifiers.add("Contact " + contact.getId() + " (" + (identifier != null ? identifier : "unknown") + ") - " + e.getMessage());
                log.error("Failed to send {} to contact {}: {}", channel, contact.getId(), e.getMessage());
            }
        }

        log.info("{} sending completed for tag {} with message {}: {} successful, {} failed", channel, tagId, messageId, successCount, failureCount);

        return new SendResult(uniqueContacts.size(), successCount, failureCount, sentIdentifiers, failedIdentifiers);
    }

    @Override
    public void sendToContact(Long contactId, Long messageId, MessageChannel channel) {
        // Validate parameters
        if (contactId == null || contactId <= 0) {
            throw new IllegalArgumentException("Invalid contact ID");
        }
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Invalid message ID");
        }
        if (channel == null) {
            throw new IllegalArgumentException("Channel is required");
        }

        // Load contact with person
        Contact contact = contactRepository.findByIdWithAssociations(contactId)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), "id", contactId.toString()));

        // Load message
        Message message = findEntityById(messageId);

        // Send message via dispatcher
        messageDispatcherService.sendToContact(contact, message, channel);

        log.info("{} sent successfully to contact {} with message {}", channel, contactId, messageId);
    }

    @Override
    public void testMessage(Long messageId, Long contactId) {
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Invalid message ID");
        }
        if (contactId == null || contactId <= 0) {
            throw new IllegalArgumentException("Invalid contact ID");
        }

        Message message = findEntityById(messageId);
        Contact contact = contactRepository.findByIdWithAssociations(contactId)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), "id", contactId.toString()));

        MessageChannel channel = message.getChannel();
        if (!messageDispatcherService.canSendToContact(contact, channel)) {
            throw new IllegalArgumentException("Contact cannot receive messages on channel " + channel);
        }

        messageDispatcherService.sendToContact(contact, message, channel);
        log.info("Test message {} sent to contact {} via {}", messageId, contactId, channel);
    }

    private void validateCreateMessage(CreateMessageDto createMessageDto) {
        if (createMessageDto == null) {
            throw new IllegalArgumentException("Message payload is required");
        }
        if (isBlank(createMessageDto.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        if (isBlank(createMessageDto.getContent())) {
            throw new IllegalArgumentException("Body is required");
        }
        if (createMessageDto.getChannel() == null) {
            throw new IllegalArgumentException("Channel is required");
        }
        if (createMessageDto.getEventId() != null && createMessageDto.getEventId() <= 0) {
            throw new IllegalArgumentException("Event id is invalid");
        }
        if (containsEventVariables(createMessageDto.getContent()) && createMessageDto.getEventId() == null) {
            throw new IllegalArgumentException("Linked event is required when message content uses event variables");
        }
        if (createMessageDto.getAttachments() != null) {
            for (int i = 0; i < createMessageDto.getAttachments().size(); i++) {
                var attachment = createMessageDto.getAttachments().get(i);
                if (attachment == null) {
                    throw new IllegalArgumentException("Attachment at index " + i + " is invalid");
                }
                if (attachment.getId() == null || attachment.getId() <= 0) {
                    throw new IllegalArgumentException("Attachment id is required at index " + i);
                }
                if (isBlank(attachment.getName())) {
                    throw new IllegalArgumentException("Attachment name is required at index " + i);
                }
                if (isBlank(attachment.getUrl())) {
                    throw new IllegalArgumentException("Attachment URL is required at index " + i);
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean containsEventVariables(String content) {
        if (isBlank(content)) {
            return false;
        }
        return EVENT_VARIABLE_PATTERN.matcher(content).find();
    }

    private List<File> resolveAttachmentEntities(List<FileDto> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = attachments.stream()
                .filter(Objects::nonNull)
                .map(FileDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<File> existingFiles = fileRepository.findAllById(ids);
        Map<Long, File> byId = existingFiles.stream().collect(Collectors.toMap(File::getId, f -> f));

        List<File> resolved = new ArrayList<>();
        for (FileDto dto : attachments) {
            if (dto == null || dto.getId() == null) {
                continue;
            }
            File file = byId.get(dto.getId());
            if (file == null) {
                throw new ObjectNotFoundException(File.class.getSimpleName(), dto.getId());
            }
            resolved.add(file);
        }
        return resolved;
    }

    private Event resolveEventForMessage(Long eventId) {
        if (eventId == null) {
            return null;
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException(Event.class.getSimpleName(), eventId));
        if (event.getStartAt() == null || !event.getStartAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Only future events can be linked to a message");
        }
        return event;
    }

    private MessageDto normalizeMessageDtoContent(MessageDto dto) {
        if (dto == null) {
            return null;
        }
        String normalizedLegacy = normalizeLegacyEventVariables(dto.getContent());
        dto.setContent(messageContentParserService.normalizeForStorage(normalizedLegacy, dto.getChannel()));
        return dto;
    }

    private String normalizeLegacyEventVariables(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return content
                .replace(LEGACY_EVENT_START_AT, EVENT_START_DATE + " " + EVENT_START_TIME)
                .replace(LEGACY_EVENT_END_AT, EVENT_END_DATE + " " + EVENT_END_TIME);
    }
}
