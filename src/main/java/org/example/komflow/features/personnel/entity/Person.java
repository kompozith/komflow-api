package org.example.komflow.features.personnel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.komflow.features.core.entity.BaseEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "prs_persons")
public abstract class Person extends BaseEntity {

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
}
