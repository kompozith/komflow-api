package org.example.komflow.features.security;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.komflow.features.core.entity.BaseEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "scr_roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "scr_role_permissions",
            joinColumns = @JoinColumn(name = "scr_role_id"),
            inverseJoinColumns = @JoinColumn(name = "scr_permission_id")
    )
    private List<Permission> permissions;
}
