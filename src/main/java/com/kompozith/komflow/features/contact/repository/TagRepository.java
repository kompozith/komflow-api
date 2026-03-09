package com.kompozith.komflow.features.contact.repository;

import com.kompozith.komflow.features.contact.dto.TagWithContactCountDto;
import com.kompozith.komflow.features.contact.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Tag> findByName(String name);

    @Query("SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.contacts c LEFT JOIN FETCH c.person p LEFT JOIN FETCH p.phoneNumbers LEFT JOIN FETCH p.user WHERE t.name = :name")
    Optional<Tag> findByNameWithContacts(@Param("name") String name);

    List<Tag> findAllByNameStartingWith(String prefix);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.contacts c LEFT JOIN FETCH c.person p LEFT JOIN FETCH p.phoneNumbers LEFT JOIN FETCH p.user WHERE t.id = :id")
    Optional<Tag> findByIdWithContacts(@Param("id") Long id);

    @Query("SELECT t, COUNT(c) FROM Tag t LEFT JOIN t.contacts c GROUP BY t")
    List<Object[]> findAllWithContactCount(org.springframework.data.domain.Sort sort);

    @Query("SELECT t, COUNT(c) FROM Tag t LEFT JOIN t.contacts c GROUP BY t.id")
    List<Object[]> findAllWithContactCount();

    @Query(value = """
    SELECT t.id, t.name, t.description, t.color_code, t.enabled, t.created_at, t.updated_at, COUNT(c.id) as contact_count
    FROM komflow.cnt_tags t
    LEFT JOIN komflow.cnt_contact_tags ct ON t.id = ct.cnt_tag_id
    LEFT JOIN komflow.cnt_contacts c ON ct.cnt_contact_id = c.id
    WHERE (COALESCE(:search, '') = ''
           OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (CAST(:createdAtFrom AS TIMESTAMP) IS NULL OR t.created_at >= CAST(:createdAtFrom AS TIMESTAMP))
      AND (CAST(:createdAtTo AS TIMESTAMP) IS NULL OR t.created_at <= CAST(:createdAtTo AS TIMESTAMP))
      AND (CAST(:enabled AS BOOLEAN) IS NULL OR t.enabled = CAST(:enabled AS BOOLEAN))
    GROUP BY t.id, t.name, t.description, t.color_code, t.enabled, t.created_at, t.updated_at
    """,
            nativeQuery = true)
    Page<TagWithContactCountDto> findWithFiltersAndContactCount(@Param("search") String search,
                                                                @Param("createdAtFrom") Instant createdAtFrom,
                                                                @Param("createdAtTo") Instant createdAtTo,
                                                                @Param("enabled") Boolean enabled,
                                                                Pageable pageable);
}
