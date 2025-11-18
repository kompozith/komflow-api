package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.PhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneNumberServiceImpl implements PhoneNumberService {

    private final PhoneNumberRepository phoneNumberRepository;
    private final PersonRepository personRepository;

    @Override
    public PhoneNumberDto addPhoneNumberToPerson(Long personId, CreatePhoneNumberDto createPhoneNumberDto) {
        // Validate person exists
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ObjectNotFoundException(Person.class.getSimpleName(), personId));

        // Check if phone number already exists
        phoneNumberRepository.findByNumber(createPhoneNumberDto.getNumber())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Phone number already exists: " + createPhoneNumberDto.getNumber());
                });

        // Create new phone number
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(createPhoneNumberDto.getNumber());
        phoneNumber.setIsWhatsapp(createPhoneNumberDto.getIsWhatsapp() != null ?
                createPhoneNumberDto.getIsWhatsapp().toString() : "false");
        phoneNumber.setPerson(person);

        // Add phone number to person's collection (maintain bidirectional relationship)
        if (person.getPhoneNumbers() == null) {
            person.setPhoneNumbers(new java.util.ArrayList<>());
        }
        person.getPhoneNumbers().add(phoneNumber);

        // Save the person (cascade will save the phone number)
        Person savedPerson = personRepository.save(person);

        // Get the saved phone number from the person's collection
        PhoneNumber savedPhoneNumber = savedPerson.getPhoneNumbers().get(savedPerson.getPhoneNumbers().size() - 1);

        log.info("Phone number {} added to person {}", createPhoneNumberDto.getNumber(), personId);

        return mapToDto(savedPhoneNumber);
    }

    @Override
    public PhoneNumberDto updatePhoneNumber(Long phoneNumberId, CreatePhoneNumberDto createPhoneNumberDto) {
        // Validate phone number exists
        PhoneNumber phoneNumber = phoneNumberRepository.findById(phoneNumberId)
                .orElseThrow(() -> new ObjectNotFoundException(PhoneNumber.class.getSimpleName(), phoneNumberId));

        // Check if new number already exists (excluding current one)
        phoneNumberRepository.findByNumber(createPhoneNumberDto.getNumber())
                .filter(existing -> !existing.getId().equals(phoneNumberId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Phone number already exists: " + createPhoneNumberDto.getNumber());
                });

        // Update phone number
        phoneNumber.setNumber(createPhoneNumberDto.getNumber());
        phoneNumber.setIsWhatsapp(createPhoneNumberDto.getIsWhatsapp() != null ?
                createPhoneNumberDto.getIsWhatsapp().toString() : "false");

        PhoneNumber savedPhoneNumber = phoneNumberRepository.save(phoneNumber);

        log.info("Phone number {} updated", phoneNumberId);

        return mapToDto(savedPhoneNumber);
    }

    @Override
    public List<PhoneNumberDto> getPhoneNumbersByPersonId(Long personId) {
        // Validate person exists
        if (!personRepository.existsById(personId)) {
            throw new ObjectNotFoundException(Person.class.getSimpleName(), personId);
        }

        return phoneNumberRepository.findByPersonId(personId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoneNumber(Long phoneNumberId) {
        PhoneNumber phoneNumber = phoneNumberRepository.findById(phoneNumberId)
                .orElseThrow(() -> new ObjectNotFoundException(PhoneNumber.class.getSimpleName(), phoneNumberId));

        // Remove from person's collection (maintain bidirectional relationship)
        Person person = phoneNumber.getPerson();
        if (person != null && person.getPhoneNumbers() != null) {
            person.getPhoneNumbers().remove(phoneNumber);
            personRepository.save(person);
        }

        phoneNumberRepository.deleteById(phoneNumberId);
        log.info("Phone number {} deleted", phoneNumberId);
    }

    private PhoneNumberDto mapToDto(PhoneNumber phoneNumber) {
        PhoneNumberDto dto = new PhoneNumberDto();
        dto.setId(phoneNumber.getId());
        dto.setNumber(phoneNumber.getNumber());
        dto.setIsWhatsapp(phoneNumber.getIsWhatsapp());
        dto.setPersonId(phoneNumber.getPerson() != null ? phoneNumber.getPerson().getId() : null);
        dto.setContactId(phoneNumber.getContact() != null ? phoneNumber.getContact().getId() : null);
        return dto;
    }
}