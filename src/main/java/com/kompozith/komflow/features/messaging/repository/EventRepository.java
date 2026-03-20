package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStartAtGreaterThanEqualOrderByStartAtAsc(Instant startAt);
    List<Event> findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(Instant rangeEnd, Instant rangeStart);
    List<Event> findAllByStartAtBetweenOrderByStartAtAsc(Instant rangeStart, Instant rangeEnd);
    Optional<Event> findBySlug(String slug);
    @Query("""
            SELECT e
            FROM Event e
            LEFT JOIN FETCH e.registrationWorkflowSteps steps
            LEFT JOIN FETCH steps.message message
            WHERE e.id = :id
            ORDER BY steps.position ASC
            """)
    Optional<Event> findByIdWithWorkflowSteps(@Param("id") Long id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}


