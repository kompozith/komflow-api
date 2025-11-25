package com.kompozith.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import com.kompozith.komflow.features.core.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "cnt_tags")
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Nom du tag
    @Column(nullable = false, unique = true)
    private String name;

    // Code couleur du tag
    @Column(name = "color_code", nullable = false)
    private String colorCode;

    // Description du tag
    private String description;

    private boolean enabled = true;

    @ManyToMany(mappedBy = "tags")
    private Set<Contact> contacts;
}
