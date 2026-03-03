package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.features.contact.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public interface ContactService {
    ContactDto create (CreateContactDto createContactDto);
    List<ContactDto> findAll();
    Page<ContactWithTagCountDto> findAll(Pageable pageable, String search, Boolean enabled, Instant createdAtFrom, Instant createdAtTo, String tagIds);
    ContactDetailsDto findById(Long id);
    ContactDto update(Long id, CreateContactDto createContactDto);
    void delete(Long id);
    byte[] exportContacts(String format, String search, Boolean enabled, Instant createdAtFrom, Instant createdAtTo, String tagIds);
    ContactImportResultDto importContacts(MultipartFile file);
    PublicEventDetailsDto getPublicEventDetails(String slug);
    PublicEventRegistrationResponseDto registerPublicEvent(String slug, PublicEventRegistrationRequestDto request);
}
