package com.kompozith.komflow.features.messaging.controller;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageTestDirectRequestDto;
import com.kompozith.komflow.features.messaging.dto.MessageTestRequestDto;
import com.kompozith.komflow.features.messaging.dto.MessageVariableDto;
import com.kompozith.komflow.features.messaging.dto.SendResult;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import com.kompozith.komflow.features.messaging.entity.MessageVariable;
import com.kompozith.komflow.features.messaging.exception.MissingChannelException;
import com.kompozith.komflow.features.messaging.service.MessageService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message Management", description = "APIs for managing messages")
public class MessageController {

    private final MessageService messageService;

    @PreAuthorize("hasAuthority('MESSAGE_CREATE')")
    @PostMapping
    @Operation(summary = "Create a new message", description = "Create a new message in the system")
    public ResponseEntity<MessageDto> create(@RequestBody CreateMessageDto createMessageDto) {
        MessageDto messageDto = messageService.create(createMessageDto);
        return ResponseEntity.ok(messageDto);
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping
    @Operation(summary = "Get all messages", description = "Retrieve a paginated list of all messages")
    public ResponseEntity<Page<MessageDto>> findAll(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) MessageChannel channel,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Instant createdAtFrom,
            @RequestParam(required = false) Instant createdAtTo) {
        Page<MessageDto> messages = messageService.findAll(pageable, channel, search, createdAtFrom, createdAtTo);
        return ResponseEntity.ok(messages);
    }

    @PreAuthorize("hasAuthority('MESSAGE_SHOW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Retrieve a specific message by its ID")
    public ResponseEntity<MessageDto> findById(@PathVariable Long id) {
        MessageDto messageDto = messageService.findById(id);
        return ResponseEntity.ok(messageDto);
    }

    @PreAuthorize("hasAuthority('MESSAGE_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update message", description = "Update an existing message by its ID")
    public ResponseEntity<MessageDto> update(@PathVariable Long id, @RequestBody CreateMessageDto createMessageDto) {
        MessageDto messageDto = messageService.update(id, createMessageDto);
        return ResponseEntity.ok(messageDto);
    }

    @PreAuthorize("hasAuthority('MESSAGE_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Delete a message by its ID")
    public ResponseEntity<SimpleResponse> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.ok(new SimpleResponse<>("Message deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping("/variables")
    @Operation(summary = "Get available message variables", description = "Retrieve a list of all available dynamic variables for message personalization")
    public ResponseEntity<List<MessageVariableDto>> getVariables() {
        List<MessageVariableDto> variables = Arrays.stream(MessageVariable.values())
                .map(variable -> new MessageVariableDto(variable.getKey(), variable.getDescription()))
                .toList();

        return ResponseEntity.ok(variables);
    }

    @PreAuthorize("hasAuthority('MESSAGE_SEND_TO_CONTACT')")
    @PostMapping("/send-to-contact/{contactId}")
    @Operation(summary = "Send message to contact", description = "Send a pre-configured message to a specific contact via specified channel")
    public ResponseEntity<SimpleResponse> sendToContact(
            @PathVariable Long contactId,
            @RequestParam Long messageId,
            @RequestParam String channel) {

        try {
            MessageChannel messageChannel = parseChannel(channel);
            messageService.sendToContact(contactId, messageId, messageChannel);
            return ResponseEntity.ok(new SimpleResponse<>(messageChannel + " sent successfully", null));
        } catch (MissingChannelException | IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new SimpleResponse<>("Failed to send message", null));
        }
    }

    @PreAuthorize("hasAuthority('MESSAGE_SEND_TO_TAG')")
    @PostMapping("/send-to-tag/{tagId}")
    @Operation(summary = "Send message to tag", description = "Send a pre-configured message to all contacts associated with a specific tag via specified channel")
    public ResponseEntity<SimpleResponse> sendToTag(
            @PathVariable Long tagId,
            @RequestParam Long messageId,
            @RequestParam String channel) {

        try {
            MessageChannel messageChannel = parseChannel(channel);
            SendResult result = messageService.sendToTag(tagId, messageId, messageChannel);
            return ResponseEntity.ok(new SimpleResponse<>(messageChannel + "s sent successfully", result));
        } catch (MissingChannelException | IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new SimpleResponse<>("Failed to send messages", null));
        }
    }

    @PreAuthorize("hasAuthority('MESSAGE_SEND_TO_CONTACT')")
    @PostMapping("/{id}/test")
    @Operation(summary = "Test a message", description = "Send a message to a specific contact for testing")
    public ResponseEntity<SimpleResponse> testMessage(
            @PathVariable Long id,
            @RequestBody MessageTestRequestDto request) {
        try {
            if (request == null || request.getContactId() == null) {
                throw new IllegalArgumentException("Contact ID is required");
            }
            messageService.testMessage(id, request.getContactId());
            return ResponseEntity.ok(new SimpleResponse<>("Test message sent successfully", null));
        } catch (MissingChannelException | IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending test message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new SimpleResponse<>("Failed to send test message", null));
        }
    }

    @PreAuthorize("hasAuthority('MESSAGE_SEND_TO_CONTACT')")
    @PostMapping("/{id}/test-direct")
    @Operation(summary = "Send a direct test message", description = "Send a test message to a given email address or phone number")
    public ResponseEntity<SimpleResponse> testMessageDirect(
            @PathVariable Long id,
            @RequestBody MessageTestDirectRequestDto request) {
        try {
            if (request == null || request.getRecipient() == null || request.getRecipient().isBlank()) {
                throw new IllegalArgumentException("Recipient is required");
            }
            messageService.testMessageDirect(id, request.getRecipient());
            return ResponseEntity.ok(new SimpleResponse<>("Test message sent successfully", null));
        } catch (MissingChannelException | IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error sending direct test message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new SimpleResponse<>("Failed to send test message", null));
        }
    }

    private MessageChannel parseChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new MissingChannelException("Channel parameter is required");
        }

        try {
            return MessageChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid channel: " + channel + ". Valid values are: EMAIL, WHATSAPP, SMS");
        }
    }
}
