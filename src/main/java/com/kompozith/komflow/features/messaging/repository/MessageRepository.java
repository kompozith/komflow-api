package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(
            value = """
        SELECT *
        FROM komflow.msg_messages m
        WHERE (:channel IS NULL OR m.channel = :channel)
          AND (
               COALESCE(:search, '') = '' 
               OR LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (CAST(:createdAtFrom AS TIMESTAMP) IS NULL OR m.created_at >= CAST(:createdAtFrom AS TIMESTAMP))
          AND (CAST(:createdAtTo AS TIMESTAMP) IS NULL OR m.created_at <= CAST(:createdAtTo AS TIMESTAMP))
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM komflow.msg_messages m
        WHERE (:channel IS NULL OR m.channel = :channel)
          AND (
               COALESCE(:search, '') = '' 
               OR LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (CAST(:createdAtFrom AS TIMESTAMP) IS NULL OR m.created_at >= CAST(:createdAtFrom AS TIMESTAMP))
          AND (CAST(:createdAtTo AS TIMESTAMP) IS NULL OR m.created_at <= CAST(:createdAtTo AS TIMESTAMP))
    """,
            nativeQuery = true
    )
    Page<Message> findWithFilters(
            @Param("channel") String channel,
            @Param("search") String search,
            @Param("createdAtFrom") Instant createdAtFrom,
            @Param("createdAtTo") Instant createdAtTo,
            Pageable pageable
    );
}
