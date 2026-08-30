package com.kompozith.komflow.features.contact.dto;

import java.time.Instant;

/**
 * Flat projection for {@link com.kompozith.komflow.features.contact.repository.ContactRepository#findWithFiltersAndTagCount}.
 * Interface projections go through Spring Data's {@code ConversionService} rather than
 * Hibernate's native-query constructor-result mapping, so JDBC types (e.g. java.sql.Timestamp)
 * are converted to the getter's declared type instead of being cast to it — safe for Instant.
 */
public interface ContactWithTagCountProjection {

    Long getId();

    Boolean getEnabled();

    Instant getLastMessageReceivedAt();

    String getCivility();

    String getProfession();

    String getAgeRange();

    String getObjectives();

    String getWebsiteUrl();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Long getTagCount();

    Long getPersonId();

    String getEmail();

    String getFirstName();

    String getLastName();

    String getLanguage();

    String getCountry();

    String getCity();

    String getTimezone();

    Instant getPersonCreatedAt();

    Instant getPersonUpdatedAt();

    String getPhoneNumber();
}
