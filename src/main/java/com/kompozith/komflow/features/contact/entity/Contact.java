package com.kompozith.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import com.kompozith.komflow.features.core.entity.BaseEntity;
import com.kompozith.komflow.features.personnel.entity.Person;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "cnt_contats")
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    @OneToOne
    @JoinColumn(name = "prs_person_id", nullable = false)
    private Person person;

    //Tags linked to the contacts
    @ManyToMany
    @JoinTable(
            name = "cnt_contact_tags",
            joinColumns = @JoinColumn(name = "cnt_contact_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private List<Tag> tags;
}
