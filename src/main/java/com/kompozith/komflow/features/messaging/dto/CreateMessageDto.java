package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageDto {
    private String title;

    private String content;

    private MessageChannel channel = MessageChannel.EMAIL;
}
