package org.example.komflow.features.messaging.entity;

import lombok.*;
import org.example.komflow.features.contact.entity.Contact;
import org.example.komflow.features.core.entity.BaseEntity;
import org.example.komflow.features.core.entity.File;
import jakarta.persistence.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "msg_messages")
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    //Objet du message
    private String title;

    // Contenue du message
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    // Pièces jointes du mail
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_message_attachments",
            joinColumns = @JoinColumn(name = "msg_message_id"),
            inverseJoinColumns = @JoinColumn(name = "gen_file_id")
    )
    private List<File> Attachments;

}
