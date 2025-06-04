package org.example.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.komflow.features.core.entity.BaseEntity;
import org.example.komflow.features.core.entity.Person;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "cnt_phone_number")
public class PhoneNumber extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "number")
    private String number;

    @Column(name = "is_number")
    private String isWhatsapp;

    @ManyToOne(cascade = CascadeType.ALL)
    private Person person;
}
