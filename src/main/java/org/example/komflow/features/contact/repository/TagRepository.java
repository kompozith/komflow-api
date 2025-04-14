package org.example.komflow.features.contact.repository;

import org.example.komflow.features.contact.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
