package org.example.komflow.features.security.repository;

import org.example.komflow.features.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
