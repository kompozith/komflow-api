package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageDto {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String content;

    @NotNull(message = "Channel is required")
    private MessageChannel channel = MessageChannel.EMAIL;
}