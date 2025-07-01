package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
