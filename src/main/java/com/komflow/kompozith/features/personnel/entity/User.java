package com.komflow.kompozith.features.personnel.entity;

import jakarta.persistence.*;
import lombok.*;
import com.komflow.kompozith.features.core.entity.BaseEntity;
import com.komflow.kompozith.features.security.entity.AuditLog;
import com.komflow.kompozith.features.security.entity.Role;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Setter
@Getter
@Table(name = "prs_users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prs_person_id", nullable = false)
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
