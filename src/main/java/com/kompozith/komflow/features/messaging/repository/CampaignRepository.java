package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("SELECT c FROM Campaign c WHERE c.status = :status AND c.scheduledAt <= :now")
    List<Campaign> findScheduledCampaignsDue(@Param("status") CampaignStatus status, @Param("now") Instant now);

    List<Campaign> findByStatus(CampaignStatus status);

    /**
     * Returns the set of distinct contact IDs targeted by a campaign, considering
     * both direct contacts and contacts that belong to any of the campaign's tags.
     * Event-registration tag contacts are computed separately in the service layer.
     */
    @Query("SELECT DISTINCT c.id FROM Campaign camp JOIN camp.contacts c WHERE camp.id = :id " +
           "UNION " +
           "SELECT DISTINCT c.id FROM Campaign camp JOIN camp.tags t JOIN t.contacts c WHERE camp.id = :id")
    Set<Long> findDirectAndTagContactIds(@Param("id") Long id);
}