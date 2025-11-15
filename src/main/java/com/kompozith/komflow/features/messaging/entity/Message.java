package com.kompozith.komflow.features.messaging.entity;

import lombok.*;
import com.kompozith.komflow.features.core.entity.BaseEntity;
import com.kompozith.komflow.features.core.entity.File;
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

    // Type du message
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // Pièces jointes du mail
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "msg_message_attachments",
            joinColumns = @JoinColumn(name = "msg_message_id"),
            inverseJoinColumns = @JoinColumn(name = "core_file_id")
    )
    private List<File> Attachments;

}
