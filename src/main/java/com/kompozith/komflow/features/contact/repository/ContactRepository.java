package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.dto.ContactWithTagCountDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("SELECT c FROM Contact c WHERE c.person.id = :personId")
    Optional<Contact> findByPersonId(@Param("personId") Long personId);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.person p LEFT JOIN FETCH p.phoneNumbers LEFT JOIN FETCH p.user LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<Contact> findByIdWithAssociations(@Param("id") Long id);

    @Query(value = """
    SELECT DISTINCT c.id, c.enabled, c.last_message_received_at, c.created_at, c.updated_at, COUNT(ct.cnt_tag_id) AS tagCount, p.id, p.email, p.first_name, p.last_name, p.language, p.country, p.city, p.timezone, p.created_at, p.updated_at, pn.number
    FROM komflow.cnt_contacts c
    LEFT JOIN komflow.prs_persons p ON p.id = c.prs_person_id
    LEFT JOIN komflow.prs_phone_number pn ON p.id = pn.person_id
    LEFT JOIN komflow.cnt_contact_tags ct ON c.id = ct.cnt_contact_id
    LEFT JOIN komflow.cnt_tags t ON t.id = ct.cnt_tag_id
    WHERE (COALESCE(:search, '') = ''
           OR LOWER(p.first_name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.last_name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(CONCAT(p.first_name, ' ', p.last_name)) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:enabled IS NULL OR c.enabled = :enabled)

      AND (CAST(:createdAtFrom AS TIMESTAMP) IS NULL OR t.created_at >= CAST(:createdAtFrom AS TIMESTAMP))
      AND (CAST(:createdAtTo AS TIMESTAMP) IS NULL OR t.created_at <= CAST(:createdAtTo AS TIMESTAMP))
      AND (
            :tagIds IS NULL
            OR t.id = ANY(CAST(:tagIds AS bigint[]))
          )
    GROUP BY c.id, c.enabled, c.last_message_received_at, c.prs_person_id, c.created_at, c.updated_at, p.id, p.first_name, p.last_name, p.email, p.language, p.country, p.city, p.timezone, p.created_at, p.updated_at, pn.number
    """, nativeQuery = true)
    Page<ContactWithTagCountDto> findWithFiltersAndTagCount(
            @Param("search") String search,
            @Param("enabled") Boolean enabled,
            @Param("createdAtFrom") Instant createdAtFrom,
            @Param("createdAtTo") Instant createdAtTo,
            @Param("tagIds") String tagIds,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT c
            FROM Contact c
            LEFT JOIN FETCH c.person p
            LEFT JOIN FETCH p.phoneNumbers
            LEFT JOIN FETCH c.tags
            WHERE c.id IN :ids
            """)
    List<Contact> findAllByIdInWithAssociations(@Param("ids") List<Long> ids);
}
