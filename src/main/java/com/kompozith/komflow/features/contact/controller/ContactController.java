package com.kompozith.komflow.features.contact.controller;

import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.ContactDto;
import com.kompozith.komflow.features.contact.dto.ContactWithTagCountDto;
import com.kompozith.komflow.features.contact.dto.CreateContactDto;
import com.kompozith.komflow.features.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/contacts")
@Tag(name = "Contact Management", description = "APIs for managing contacts")
public class ContactController {

    private final ContactService contactService;

    @PreAuthorize("hasAuthority('CONTACT_CREATE')")
    @PostMapping
    @Operation(summary = "Create a new contact", description = "Create a new contact in the system")
    public ContactDto create(@Valid @RequestBody CreateContactDto createContactDto) {
        return contactService.create(createContactDto);
    }

    @PreAuthorize("hasAuthority('CONTACT_LIST')")
    @GetMapping
    @Operation(summary = "Get all contacts", description = "Retrieve a paginated list of contacts with optional search, date and status filters")
    public Page<ContactWithTagCountDto> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Instant createdAtFrom,
            @RequestParam(required = false) Instant createdAtTo,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String tagIds) {

        Pageable pageable = PageRequest.of(page, size);
        return contactService.findAll(pageable, search, enabled, createdAtFrom, createdAtTo, tagIds);
    }

    @PreAuthorize("hasAuthority('CONTACT_SHOW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get contact by ID", description = "Retrieve a specific contact by its ID")
    public ContactDetailsDto findById(@PathVariable Long id) {
        return contactService.findById(id);
    }

    @PreAuthorize("hasAuthority('CONTACT_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update contact", description = "Update an existing contact by its ID")
    public ContactDto update(@PathVariable Long id, @RequestBody CreateContactDto createContactDto) {
        return contactService.update(id, createContactDto);
    }

    @PreAuthorize("hasAuthority('CONTACT_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete contact", description = "Delete a contact by its ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
