package com.kompozith.komflow.features.contact.dto;

import java.time.Instant;

/**
 * Flat projection for {@link com.kompozith.komflow.features.contact.repository.TagRepository#findWithFiltersAndContactCount}.
 * Interface projections go through Spring Data's {@code ConversionService} rather than
 * Hibernate's native-query constructor-result mapping, so JDBC types (e.g. java.sql.Timestamp)
 * are converted to the getter's declared type instead of being cast to it — safe for Instant.
 */
public interface TagWithContactCountProjection {

    Long getId();

    String getName();

    String getDescription();

    String getColorCode();

    Boolean getEnabled();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Long getContactCount();
}
