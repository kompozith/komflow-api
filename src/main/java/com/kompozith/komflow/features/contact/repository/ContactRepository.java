package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("SELECT c FROM Contact c WHERE c.person.id = :personId")
    Optional<Contact> findByPersonId(@Param("personId") Long personId);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.person LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<Contact> findByIdWithAssociations(@Param("id") Long id);
}
