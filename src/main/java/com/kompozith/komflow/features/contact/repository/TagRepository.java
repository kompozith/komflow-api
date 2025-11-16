package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.contacts WHERE t.id = :id")
    Optional<Tag> findByIdWithContacts(@Param("id") Long id);
}
