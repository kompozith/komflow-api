package org.example.komflow.features.personnel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.komflow.features.core.entity.BaseEntity;
import org.example.komflow.features.security.entity.AuditLog;
import org.example.komflow.features.security.entity.Role;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "prs_users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String username;

    private String password;

    private boolean enabled;

    @OneToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "prs_user_roles",
            joinColumns = @JoinColumn(name = "prs_user_id"),
            inverseJoinColumns = @JoinColumn(name = "scr_role_id")
    )
    private List<Role> roles;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AuditLog> logs;
}
