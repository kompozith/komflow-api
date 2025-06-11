package com.komflow.kompozith.features.personnel.entity;

import com.komflow.kompozith.features.contact.entity.Contact;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.komflow.kompozith.features.core.entity.BaseEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Builder
@Entity
@Table(name = "prs_persons")
public class Person extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Email du contact
    private String email;

    // Prenom du contact
    @Column(name = "first_name", nullable = false)
    private String firstName;

    // Nom du contact
    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String language;

    @OneToMany(cascade = CascadeType.ALL)
    private List<PhoneNumber> phoneNumbers;

    @OneToOne(cascade = CascadeType.ALL)
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private Contact contact;
}
