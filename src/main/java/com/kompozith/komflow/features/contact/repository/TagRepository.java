package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Tag> findByName(String name);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.contacts c LEFT JOIN FETCH c.person p LEFT JOIN FETCH p.phoneNumbers LEFT JOIN FETCH p.user WHERE t.id = :id")
    Optional<Tag> findByIdWithContacts(@Param("id") Long id);

    @Query("SELECT t, COUNT(c) FROM Tag t LEFT JOIN t.contacts c GROUP BY t")
    List<Object[]> findAllWithContactCount(org.springframework.data.domain.Sort sort);

    @Query("SELECT t, COUNT(c) FROM Tag t LEFT JOIN t.contacts c GROUP BY t.id")
    List<Object[]> findAllWithContactCount();
}
