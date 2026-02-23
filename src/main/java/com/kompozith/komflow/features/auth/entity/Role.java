package com.kompozith.komflow.features.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.kompozith.komflow.features.core.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "aut_roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = true, columnDefinition = "varchar(255) default 'CUSTOM'")
    private RoleType type = RoleType.CUSTOM;

    @Column(name = "active", nullable = true, columnDefinition = "boolean default true")
    private Boolean active = true;

    @ElementCollection
    @CollectionTable(
            name = "aut_role_permissions",
            joinColumns = @JoinColumn(name = "aut_role_id")
    )
    @Column(name = "aut_permission_code")
    private Set<String> permissions = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void ensureTypeDefault() {
        if (type == null) {
            type = RoleType.CUSTOM;
        }
        if (active == null) {
            active = true;
        }
    }
}
