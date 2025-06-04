package org.example.komflow.features.messaging.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.komflow.features.contact.entity.Contact;
import org.example.komflow.features.core.entity.BaseEntity;

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

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Contact> contacts;

    // Cc du mail
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_email_cc",
            joinColumns = @JoinColumn(name = "msg_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCc;

    // Cci du mail
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_email_cci",
            joinColumns = @JoinColumn(name = "msg_ampaign_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCci;
}
