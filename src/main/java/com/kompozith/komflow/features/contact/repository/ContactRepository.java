package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
