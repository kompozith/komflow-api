package com.kompozith.komflow.features.security.repository;

import com.kompozith.komflow.features.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
