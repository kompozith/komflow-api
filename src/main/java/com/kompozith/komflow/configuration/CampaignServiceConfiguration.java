package com.kompozith.komflow.configuration;

import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.messaging.mapper.CampaignMapper;
import com.kompozith.komflow.features.messaging.repository.CampaignContactResultRepository;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import com.kompozith.komflow.features.messaging.service.CampaignExecutionService;
import com.kompozith.komflow.features.messaging.service.CampaignService;
import com.kompozith.komflow.features.messaging.service.CampaignServiceImpl;
import com.kompozith.komflow.features.messaging.service.MessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CampaignServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean(CampaignService.class)
    public CampaignService campaignService(
            CampaignRepository campaignRepository,
            CampaignMapper campaignMapper,
            MessageService messageService,
            ContactRepository contactRepository,
            TagRepository tagRepository,
            CampaignExecutionService campaignExecutionService,
            CampaignContactResultRepository campaignContactResultRepository
    ) {
        return new CampaignServiceImpl(
                campaignRepository,
                campaignMapper,
                messageService,
                contactRepository,
                tagRepository,
                campaignExecutionService,
                campaignContactResultRepository
        );
    }
}


