package com.kompozith.komflow.features.personnel.entity;

import com.kompozith.komflow.features.auth.entity.AuditLog;
import com.kompozith.komflow.features.auth.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import com.kompozith.komflow.features.core.entity.BaseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @ManyToMany
    @JoinTable(
            name = "prs_user_roles",
            joinColumns = @JoinColumn(name = "prs_user_id"),
            inverseJoinColumns = @JoinColumn(name = "aut_role_id")
    )
    private Set<Role> roles;

    @OneToMany(cascade = CascadeType.ALL)
    private List<AuditLog> logs;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
