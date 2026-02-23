package com.kompozith.komflow.features.auth.repository;

import com.kompozith.komflow.features.auth.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    public Optional<Role> findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("""
            SELECT r FROM Role r
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Role> search(@Param("search") String search, Pageable pageable);

    List<Role> findByTypeIsNull();

    List<Role> findByActiveIsNull();
}
