package com.kompozith.komflow.features.personnel.controller;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;
import com.kompozith.komflow.features.personnel.service.PhoneNumberService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final PhoneNumberService phoneNumberService;

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