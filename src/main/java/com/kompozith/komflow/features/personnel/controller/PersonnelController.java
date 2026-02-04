package com.kompozith.komflow.features.personnel.controller;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.PersonDetailsDto;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.UpdatePersonDto;
import com.kompozith.komflow.features.personnel.service.PersonService;
import com.kompozith.komflow.features.personnel.service.PhoneNumberService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/personnel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Personnel Management", description = "APIs for managing personnel data")
public class PersonnelController {

    private final PersonService personService;
    private final PhoneNumberService phoneNumberService;

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PostMapping("/persons")
    @Operation(summary = "Create a new person", description = "Create a new person in the system")
    public PersonDto createPerson(@Valid @RequestBody CreatePersonDto createPersonDto) {
        return personService.create(createPersonDto);
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/persons")
    @Operation(summary = "Get all persons", description = "Retrieve a paginated list of persons with optional search")
    public Page<PersonDto> findAllPersons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return personService.findAll(pageable, search);
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/persons/{id}")
    @Operation(summary = "Get person by ID", description = "Retrieve a specific person by its ID")
    public PersonDetailsDto findPersonById(@PathVariable Long id) {
        return personService.findById(id);
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PutMapping("/persons/{id}")
    @Operation(summary = "Update person", description = "Update an existing person by its ID")
    public PersonDto updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePersonDto updatePersonDto) {
        return personService.update(id, updatePersonDto);
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PostMapping("/{personId}/phone-numbers")
    @Operation(summary = "Add phone number to person", description = "Add a new phone number to a specific person")
    public ResponseEntity<PhoneNumberDto> addPhoneNumberToPerson(
            @PathVariable Long personId,
            @Valid @RequestBody CreatePhoneNumberDto createPhoneNumberDto) {

        try {
            PhoneNumberDto phoneNumberDto = phoneNumberService.addPhoneNumberToPerson(personId, createPhoneNumberDto);
            return ResponseEntity.ok(phoneNumberDto);
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error adding phone number: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @PutMapping("/phone-numbers/{phoneNumberId}")
    @Operation(summary = "Update phone number", description = "Update an existing phone number")
    public ResponseEntity<PhoneNumberDto> updatePhoneNumber(
            @PathVariable Long phoneNumberId,
            @Valid @RequestBody CreatePhoneNumberDto createPhoneNumberDto) {

        try {
            PhoneNumberDto phoneNumberDto = phoneNumberService.updatePhoneNumber(phoneNumberId, createPhoneNumberDto);
            return ResponseEntity.ok(phoneNumberDto);
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error updating phone number: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PreAuthorize("hasAuthority('PERSONNEL_VIEW')")
    @GetMapping("/{personId}/phone-numbers")
    @Operation(summary = "Get phone numbers by person", description = "Retrieve all phone numbers for a specific person")
    public ResponseEntity<List<PhoneNumberDto>> getPhoneNumbersByPerson(@PathVariable Long personId) {
        try {
            List<PhoneNumberDto> phoneNumbers = phoneNumberService.getPhoneNumbersByPersonId(personId);
            return ResponseEntity.ok(phoneNumbers);
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error retrieving phone numbers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PreAuthorize("hasAuthority('PERSONNEL_MANAGE')")
    @DeleteMapping("/phone-numbers/{phoneNumberId}")
    @Operation(summary = "Delete phone number", description = "Delete a specific phone number")
    public ResponseEntity<SimpleResponse> deletePhoneNumber(@PathVariable Long phoneNumberId) {
        try {
            phoneNumberService.deletePhoneNumber(phoneNumberId);
            return ResponseEntity.ok(new SimpleResponse<>("Phone number deleted successfully", null));
        } catch (ObjectNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new SimpleResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting phone number: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new SimpleResponse<>("Failed to delete phone number", null));
        }
    }
}
