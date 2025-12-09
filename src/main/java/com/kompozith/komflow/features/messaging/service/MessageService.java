package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.dto.SendResult;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface MessageService {
    MessageDto create(CreateMessageDto createMessageDto);
    List<MessageDto> findAll();
    Page<MessageDto> findAll(Pageable pageable);
    Page<MessageDto> findAll(Pageable pageable, MessageChannel channel, String search, Instant createdAtFrom, Instant createdAtTo);
    MessageDto findById(Long id);
    MessageDto update(Long id, CreateMessageDto createMessageDto);
    void delete(Long id);
    Message findEntityById(Long id);
    void sendToContact(Long contactId, Long messageId, MessageChannel channel);
    SendResult sendToTag(Long tagId, Long messageId, MessageChannel channel);
}