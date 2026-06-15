package com.kompozith.komflow.features.messaging.entity;

import com.kompozith.komflow.features.contact.entity.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.core.entity.BaseEntity;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "msg_campaigns")
public class Campaign extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Message message;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Contact> contacts;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Tag> tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    private Instant scheduledAt;

    // Cc du mail
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "msg_email_cc_contacts",
            joinColumns = @JoinColumn(name = "msg_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCcContacts;

    // Cci du mail
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "msg_email_cci_contacts",
            joinColumns = @JoinColumn(name = "msg_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCciContacts;

    // Tags pour CC du mail
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "msg_campaign_cc_tags",
            joinColumns = @JoinColumn(name = "msg_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private List<com.kompozith.komflow.features.contact.entity.Tag> mailCcTags;

    // Tags pour CCI du mail
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "msg_campaign_cci_tags",
            joinColumns = @JoinColumn(name = "msg_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private List<com.kompozith.komflow.features.contact.entity.Tag> mailCciTags;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
}
