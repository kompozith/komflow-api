package org.example.komflow.features.messaging.entity;

import lombok.*;
import org.example.komflow.features.contact.entity.Contact;
import org.example.komflow.features.general.entity.BaseEntity;
import org.example.komflow.features.general.entity.File;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Data
@Table(name = "msg_messages")
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    private Long id;

    //Objet du message
    private String object;

    @ManyToOne
    @JoinColumn(name = "cnt_contact_from")
    private Contact contactFrom;

    @ManyToOne
    @JoinColumn(name = "cnt_contact_to")
    private Contact contactTo;

    // Aperçu du message
    private String preview;

    // Contenue du message
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Cc du mail
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_email_cc",
            joinColumns = @JoinColumn(name = "msg_message_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCc;

    // Cci du mail
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_email_cci",
            joinColumns = @JoinColumn(name = "msg_message_id"),
            inverseJoinColumns = @JoinColumn(name = "cnt_contact_id")
    )
    private List<Contact> mailCci;

    // Pièces jointes du mail
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_message_attachments",
            joinColumns = @JoinColumn(name = "msg_message_id"),
            inverseJoinColumns = @JoinColumn(name = "gen_file_id")
    )
    private List<File> Attachments;

}
