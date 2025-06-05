package org.example.komflow.features.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;

    @NotBlank(message = "Action cannot be blank")
    @Size(max = 100, message = "Action cannot exceed 100 characters")
    private String action;

    @Size(max = 100, message = "Object type cannot exceed 100 characters")
    private String objectType;

    @Size(max = 100, message = "Object ID cannot exceed 100 characters")
    private String objectId;

    @Size(max = 50, message = "IP address cannot exceed 50 characters")
    private String ipAddress;

    @Size(max = 255, message = "User agent cannot exceed 255 characters")
    private String userAgent;

    private String details; // Could be large, so no size constraint for now

    private Instant date; // Timestamp of the audit log
}
