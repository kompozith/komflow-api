package org.example.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.komflow.features.core.entity.Person;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "cnt_contats")
public class Contact extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private boolean enabled;

    private Instant lastMessageReceivedAt;

    @OneToOne(cascade = CascadeType.ALL)
    private Person person;

    //Tags linked to the contacts
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "cnt_contact_tags",
            joinColumns = @JoinColumn(name = "cnt_contact_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private List<Tag> tags;
}
