package features.contact.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "cnt_contats")
public class Contact {

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

    // Date de création (automatique)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Dernière modification (mise à jour automatique)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
