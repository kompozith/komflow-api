package org.example.komflow.features.personnel.repository;

import org.example.komflow.features.personnel.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
