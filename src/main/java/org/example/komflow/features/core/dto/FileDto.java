package org.example.komflow.features.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
// Removed unused NotNull import from previous version
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {

    private Long id;

    @NotBlank(message = "file.name.notBlank")
    private String name;

    @NotBlank(message = "file.url.notBlank")
    @URL(message = "file.url.invalidFormat")
    private String url;

    private Instant createdAt;

    private Instant updatedAt;
}
