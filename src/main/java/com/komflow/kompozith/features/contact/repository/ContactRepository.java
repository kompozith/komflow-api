package com.komflow.kompozith.features.contact.repository;

import com.komflow.kompozith.features.contact.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
