package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.CampaignContactResult;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignContactResultRepository extends JpaRepository<CampaignContactResult, Long> {

    @Query(
            value = "SELECT r FROM CampaignContactResult r " +
                    "JOIN FETCH r.contact c JOIN FETCH c.person p " +
                    "WHERE r.campaign.id = :campaignId",
            countQuery = "SELECT COUNT(r) FROM CampaignContactResult r WHERE r.campaign.id = :campaignId"
    )
    Page<CampaignContactResult> findByCampaignId(
            @Param("campaignId") Long campaignId, Pageable pageable);

    @Query(
            value = "SELECT r FROM CampaignContactResult r " +
                    "JOIN FETCH r.contact c JOIN FETCH c.person p " +
                    "WHERE r.campaign.id = :campaignId AND r.status = :status",
            countQuery = "SELECT COUNT(r) FROM CampaignContactResult r " +
                    "WHERE r.campaign.id = :campaignId AND r.status = :status"
    )
    Page<CampaignContactResult> findByCampaignIdAndStatus(
            @Param("campaignId") Long campaignId,
            @Param("status") CampaignSendStatus status,
            Pageable pageable);

    long countByCampaignIdAndStatus(Long campaignId, CampaignSendStatus status);

    long countByCampaignId(Long campaignId);
}

