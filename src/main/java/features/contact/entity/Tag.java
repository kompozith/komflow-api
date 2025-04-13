package features.contact.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "cnt_tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    private Long id;

    // Nom du tag
    @Column(nullable = false)
    private String name;

    // Code couleur du tag
    @Column(name = "color_code", nullable = false)
    private String colorCode;

    // Description du tag
    private String description;

    @ManyToMany(mappedBy = "tags")
    private List<Contact> contacts;

    // Date de création (automatique)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Dernière modification (mise à jour automatique)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
