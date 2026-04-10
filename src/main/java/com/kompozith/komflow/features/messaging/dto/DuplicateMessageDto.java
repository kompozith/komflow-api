package com.kompozith.komflow.features.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de la requête de duplication d'un message.
 * Seul le titre du doublon est requis ; tout le reste est copié depuis l'original.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateMessageDto {

    @NotBlank(message = "Le titre du message dupliqué est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String title;
}
