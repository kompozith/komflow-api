package com.kompozith.komflow.features.messaging.mapper;

import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "attachmentCount", expression = "java(message.getAttachments() != null ? message.getAttachments().size() : 0)")
    MessageDto messageToMessageDto(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Message createMessageDtoToMessage(CreateMessageDto createMessageDto);
}