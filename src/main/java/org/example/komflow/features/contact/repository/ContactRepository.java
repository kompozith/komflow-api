package org.example.komflow.features.contact.repository;

import org.example.komflow.features.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
