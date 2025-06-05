package org.example.komflow.features.messaging.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.komflow.features.core.dto.FileDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long id;

    @NotBlank(message = "message.title.notBlank")
    @Size(max = 255, message = "message.title.sizeExceeded")
    private String title;

    @NotBlank(message = "message.body.notBlank")
    private String body;

    private List<FileDto> attachments; // Using existing FileDto

    private Instant createdAt;

    private Instant updatedAt;
}
