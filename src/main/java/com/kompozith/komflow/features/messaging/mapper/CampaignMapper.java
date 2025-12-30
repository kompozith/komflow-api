package com.kompozith.komflow.features.messaging.mapper;

import com.kompozith.komflow.features.contact.mapper.ContactMapper;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MessageMapper.class, ContactMapper.class, TagMapper.class})
public interface CampaignMapper {

    @Mapping(target = "contactIds", expression = "java(campaign.getContacts() != null ? campaign.getContacts().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "tagIds", expression = "java(campaign.getTags() != null ? campaign.getTags().stream().map(t -> t.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCcContactIds", expression = "java(campaign.getMailCcContacts() != null ? campaign.getMailCcContacts().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCciContactIds", expression = "java(campaign.getMailCciContacts() != null ? campaign.getMailCciContacts().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCcTagIds", expression = "java(campaign.getMailCcTags() != null ? campaign.getMailCcTags().stream().map(t -> t.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCciTagIds", expression = "java(campaign.getMailCciTags() != null ? campaign.getMailCciTags().stream().map(t -> t.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    CampaignDto campaignToCampaignDto(Campaign campaign);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "mailCcContacts", ignore = true)
    @Mapping(target = "mailCciContacts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Campaign createCampaignDtoToCampaign(CreateCampaignDto createCampaignDto);

    CampaignDetailsDto campaignToCampaignDetailsDto(Campaign campaign);
}