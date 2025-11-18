package com.kompozith.komflow.features.messaging.mapper;

import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MessageMapper.class})
public interface CampaignMapper {

    @Mapping(target = "contactIds", expression = "java(campaign.getContacts() != null ? campaign.getContacts().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCcIds", expression = "java(campaign.getMailCc() != null ? campaign.getMailCc().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    @Mapping(target = "mailCciIds", expression = "java(campaign.getMailCci() != null ? campaign.getMailCci().stream().map(c -> c.getId()).collect(java.util.stream.Collectors.toList()) : null)")
    CampaignDto campaignToCampaignDto(Campaign campaign);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "mailCc", ignore = true)
    @Mapping(target = "mailCci", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Campaign createCampaignDtoToCampaign(CreateCampaignDto createCampaignDto);
}