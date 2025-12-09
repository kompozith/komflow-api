package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private String title;
    private String content;
    private MessageChannel channel;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer attachmentCount;
}
