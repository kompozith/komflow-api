package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.ContactDto;
import com.kompozith.komflow.features.contact.dto.CreateContactDto;

import java.util.List;

public interface ContactService {
    ContactDto create (CreateContactDto createContactDto);
    List<ContactDto> findAll();
    ContactDetailsDto findById(Long id);
    ContactDto update(Long id, CreateContactDto createContactDto);
    void delete(Long id);
}