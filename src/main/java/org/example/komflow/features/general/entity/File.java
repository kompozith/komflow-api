package org.example.komflow.features.general.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Data
@Table(name = "gen_files")
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    private Long id;

    //Objet du message
    @Column(nullable = false)
    private String name;

    // Aperçu du message
    @Column(nullable = false)
    private String url;
}
