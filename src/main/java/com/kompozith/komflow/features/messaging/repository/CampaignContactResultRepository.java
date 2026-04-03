package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.CampaignContactResult;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

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

    // ── Search-aware variants ──────────────────────────────────────────────────

    @Query(
            value = "SELECT r FROM CampaignContactResult r " +
                    "JOIN FETCH r.contact c JOIN FETCH c.person p " +
                    "WHERE r.campaign.id = :campaignId " +
                    "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(r) FROM CampaignContactResult r " +
                    "JOIN r.contact c JOIN c.person p " +
                    "WHERE r.campaign.id = :campaignId " +
                    "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<CampaignContactResult> findByCampaignIdWithSearch(
            @Param("campaignId") Long campaignId,
            @Param("search") String search,
            Pageable pageable);

    @Query(
            value = "SELECT r FROM CampaignContactResult r " +
                    "JOIN FETCH r.contact c JOIN FETCH c.person p " +
                    "WHERE r.campaign.id = :campaignId AND r.status = :status " +
                    "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(r) FROM CampaignContactResult r " +
                    "JOIN r.contact c JOIN c.person p " +
                    "WHERE r.campaign.id = :campaignId AND r.status = :status " +
                    "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                    "     OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<CampaignContactResult> findByCampaignIdAndStatusWithSearch(
            @Param("campaignId") Long campaignId,
            @Param("status") CampaignSendStatus status,
            @Param("search") String search,
            Pageable pageable);

    long countByCampaignIdAndStatus(Long campaignId, CampaignSendStatus status);

    long countByCampaignId(Long campaignId);

    /**
     * Lookup a single result row by campaign + contact (used for upsert).
     * The unique constraint {@code uq_ccr_campaign_contact} guarantees at most
     * one row per pair, so an {@link Optional} is the correct return type.
     */
    @Query("SELECT r FROM CampaignContactResult r " +
           "WHERE r.campaign.id = :campaignId AND r.contact.id = :contactId")
    Optional<CampaignContactResult> findByCampaignIdAndContactId(
            @Param("campaignId") Long campaignId,
            @Param("contactId")  Long contactId);

    /**
     * Returns the set of contact IDs that already have the given send status for
     * the specified campaign. Used by the resubmit flow to skip contacts that
     * were successfully reached in a previous execution.
     */
    @Query("SELECT r.contact.id FROM CampaignContactResult r " +
           "WHERE r.campaign.id = :campaignId AND r.status = :status")
    Set<Long> findContactIdsByCampaignIdAndStatus(
            @Param("campaignId") Long campaignId,
            @Param("status") CampaignSendStatus status);

    /**
     * Returns the {@code createdAt} timestamp of the most recent result row for
     * the given campaign. Used by the stale-RUNNING watcher to detect orphaned
     * executions (execution thread crashed after some rows were persisted).
     */
    @Query("SELECT MAX(r.createdAt) FROM CampaignContactResult r WHERE r.campaign.id = :campaignId")
    java.time.Instant findLastResultCreatedAt(@Param("campaignId") Long campaignId);
}
