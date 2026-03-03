package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStartAtGreaterThanEqualOrderByStartAtAsc(Instant startAt);
    List<Event> findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(Instant rangeEnd, Instant rangeStart);
    List<Event> findAllByStartAtBetweenOrderByStartAtAsc(Instant rangeStart, Instant rangeEnd);
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}
