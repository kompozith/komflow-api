package org.example.komflow.features.general.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "gen_files")
public class File {

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
