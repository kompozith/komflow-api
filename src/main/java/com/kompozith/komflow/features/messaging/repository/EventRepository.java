package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStartAtGreaterThanEqualOrderByStartAtAsc(Instant startAt);
    List<Event> findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(Instant rangeEnd, Instant rangeStart);
    List<Event> findAllByStartAtBetweenOrderByStartAtAsc(Instant rangeStart, Instant rangeEnd);
}
