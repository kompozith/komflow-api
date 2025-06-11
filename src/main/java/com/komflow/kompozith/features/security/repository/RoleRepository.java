package com.komflow.kompozith.features.security.repository;

import com.komflow.kompozith.features.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
