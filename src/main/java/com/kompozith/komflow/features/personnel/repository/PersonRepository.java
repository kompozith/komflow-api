package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);

    @Query("""
        SELECT p FROM Person p
        WHERE (:search IS NULL OR :search = ''
           OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Person> findAllBySearch(@Param("search") String search, Pageable pageable);
}
