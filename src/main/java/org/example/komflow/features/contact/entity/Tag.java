package org.example.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.komflow.features.general.entity.BaseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Data
@Table(name = "cnt_tags")
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    private Long id;

    // Nom du tag
    @Column(nullable = false, unique = true)
    private String name;

    // Code couleur du tag
    @Column(name = "color_code", nullable = false)
    private String colorCode;

    // Description du tag
    private String description;

    @ManyToMany(mappedBy = "tags")
    private List<Contact> contacts;
}
