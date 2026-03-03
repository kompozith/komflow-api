package com.kompozith.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import com.kompozith.komflow.features.core.entity.BaseEntity;
import com.kompozith.komflow.features.personnel.entity.Person;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "cnt_contacts")
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    private String civility;

    private String profession;

    private String ageRange;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    private String websiteUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "prs_person_id", nullable = false)
    private Person person;

    //Tags linked to the contacts
    @ManyToMany
    @JoinTable(
            name = "cnt_contact_tags",
            joinColumns = @JoinColumn(name = "cnt_contact_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private Set<Tag> tags;
}
