package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);
}
