package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.dto.SendResult;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.mapper.MessageMapper;
import com.kompozith.komflow.features.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ContactRepository contactRepository;
    private final TagRepository tagRepository;
    private final MessageDispatcherService messageDispatcherService;
    private final EmailService emailService; // Keep for backward compatibility

    @Override
    public MessageDto create(CreateMessageDto createMessageDto) {
        Message message = messageMapper.createMessageDtoToMessage(createMessageDto);
        Message savedMessage = messageRepository.save(message);

        log.info("Message created with id: {}", savedMessage.getId());
        return messageMapper.messageToMessageDto(savedMessage);
    }

    @Override
    public List<MessageDto> findAll() {
        return messageRepository.findAll().stream()
                .map(messageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageDto> findAll(Pageable pageable) {
        return messageRepository.findAll(pageable)
                .map(messageMapper::messageToMessageDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> findAll(Pageable pageable, MessageChannel channel, String search, Instant createdAtFrom, Instant createdAtTo) {

        return messageRepository.findWithFilters(
                channel != null ? channel.name() : null,
                search,
                createdAtFrom,
                createdAtTo,
                pageable
        ).map(messageMapper::messageToMessageDto);
    }

    @Override
    public MessageDto findById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), id));
        return messageMapper.messageToMessageDto(message);
    }

    @Override
    public MessageDto update(Long id, CreateMessageDto createMessageDto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Message.class.getSimpleName(), id));

        message.setTitle(createMessageDto.getTitle());
        message.setContent(createMessageDto.getContent());
        message.setChannel(createMessageDto.getChannel());

        Message updatedMessage = messageRepository.save(message);

        log.info("Message updated with id: {}", id);
        return messageMapper.messageToMessageDto(updatedMessage);
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
}