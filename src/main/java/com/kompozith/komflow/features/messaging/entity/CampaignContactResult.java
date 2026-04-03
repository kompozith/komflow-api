package com.kompozith.komflow.features.messaging.entity;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Tracks the send result for each contact processed by a campaign execution.
 * Persisted immediately after each send attempt (success or failure) so that
 * the full dispatch log survives partial failures, restarts or auth outages.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(
        name = "msg_campaign_contact_results",
        indexes = {
                @Index(name = "idx_ccr_campaign_id",        columnList = "campaign_id"),
                @Index(name = "idx_ccr_campaign_id_status", columnList = "campaign_id, status")
        }
)
public class CampaignContactResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /** The campaign this result belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    /**
     * The targeted contact.
     * Stored as FK (not email) to avoid redundant data and keep referential integrity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    /** Channel used (EMAIL, SMS, WHATSAPP). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel;

    /** SUCCESS when the SMTP server accepted the message, FAILED otherwise. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CampaignSendStatus status;

    /** Populated only when status = FAILED. Contains the root error message. */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}

