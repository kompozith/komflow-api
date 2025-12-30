package com.kompozith.komflow.features.messaging.dto;

import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.TagWithContactCountDto;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDetailsDto {
    private Long id;
    private String name;
    private String description;
    private MessageDto message;
    private List<ContactDetailsDto> contacts;
    private List<TagWithContactCountDto> tags;
    private List<ContactDetailsDto> mailCcContacts;
    private List<TagWithContactCountDto> mailCcTags;
    private List<ContactDetailsDto> mailCciContacts;
    private List<TagWithContactCountDto> mailCciTags;
    private CampaignStatus status;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;
}