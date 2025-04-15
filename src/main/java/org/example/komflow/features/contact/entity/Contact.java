package org.example.komflow.features.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.komflow.features.general.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Data
@Table(name = "cnt_contats")
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    private Long id;

    // Email du contact
    private String email;

    // Prenom du contact
    @Column(name = "first_name", nullable = false)
    private String firstName;

    // Nom du contact
    @Column(name = "last_name", nullable = false)
    private String lastName;

    // numero de telephone du contact
    @Column(name = "phone_number")
    private String phoneNumber;

    // Numero Whatsapp du contact
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    //Tags linked to the contacts
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "cnt_contact_tags",
            joinColumns = @JoinColumn(name = "cnt_contact_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_tag_id")
    )
    private List<Tag> tags;
}
