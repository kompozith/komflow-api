package com.kompozith.komflow.features.personnel.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.PersonDetailsDto;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.dto.PhoneNumberDto;
import com.kompozith.komflow.features.personnel.dto.UpdatePersonDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public PersonDto create(CreatePersonDto createPersonDto) {
        if (personRepository.findByEmail(createPersonDto.getEmail()).isPresent()) {
            throw new ObjectExistException(Person.class.getSimpleName(), "email", createPersonDto.getEmail());
        }

        Person person = new Person();
        person.setEmail(createPersonDto.getEmail());
        person.setFirstName(createPersonDto.getFirstName());
        person.setLastName(createPersonDto.getLastName());
        person.setLanguage(normalizeLanguage(createPersonDto.getLanguage()));
        person.setCountry(createPersonDto.getCountry());
        person.setCity(createPersonDto.getCity());
        person.setTimezone(createPersonDto.getTimezone());

        Person saved = personRepository.save(person);
        return mapToPersonDto(saved);
    }

    @Override
    public Page<PersonDto> findAll(Pageable pageable, String search) {
        return personRepository.findAllBySearch(search, pageable)
                .map(this::mapToPersonDto);
    }

    @Override
    public PersonDetailsDto findById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Person.class.getSimpleName(), id));
        return mapToPersonDetailsDto(person);
    }

    @Override
    public PersonDto update(Long id, UpdatePersonDto updatePersonDto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Person.class.getSimpleName(), id));

        personRepository.findByEmail(updatePersonDto.getEmail())
                .filter(existing -> !existing.getId().equals(person.getId()))
                .ifPresent(existing -> {
                    throw new ObjectExistException(Person.class.getSimpleName(), "email", updatePersonDto.getEmail());
                });

        person.setEmail(updatePersonDto.getEmail());
        person.setFirstName(updatePersonDto.getFirstName());
        person.setLastName(updatePersonDto.getLastName());
        person.setLanguage(normalizeLanguage(updatePersonDto.getLanguage()));
        person.setCountry(updatePersonDto.getCountry());
        person.setCity(updatePersonDto.getCity());
        person.setTimezone(updatePersonDto.getTimezone());

        Person saved = personRepository.save(person);
        return mapToPersonDto(saved);
    }

    private PersonDto mapToPersonDto(Person person) {
        String primaryPhone = null;
        if (person.getPhoneNumbers() != null && !person.getPhoneNumbers().isEmpty()) {
            primaryPhone = person.getPhoneNumbers().get(0).getNumber();
        }
        return new PersonDto(
                person.getId(),
                person.getEmail(),
                person.getFirstName(),
                person.getLastName(),
                person.getLanguage(),
                person.getCountry(),
                person.getCity(),
                person.getTimezone(),
                primaryPhone,
                person.getCreatedAt(),
                person.getUpdatedAt()
        );
    }

    private PersonDetailsDto mapToPersonDetailsDto(Person person) {
        List<PhoneNumberDto> phoneNumbers = person.getPhoneNumbers() == null
                ? List.of()
                : person.getPhoneNumbers().stream()
                    .map(this::mapToPhoneNumberDto)
                    .toList();

        return new PersonDetailsDto(
                person.getId(),
                person.getEmail(),
                person.getFirstName(),
                person.getLastName(),
                person.getLanguage(),
                person.getCountry(),
                person.getCity(),
                person.getTimezone(),
                phoneNumbers,
                person.getCreatedAt(),
                person.getUpdatedAt()
        );
    }

    private PhoneNumberDto mapToPhoneNumberDto(PhoneNumber phoneNumber) {
        return new PhoneNumberDto(
                phoneNumber.getId(),
                phoneNumber.getNumber(),
                phoneNumber.getIsWhatsapp(),
                phoneNumber.getPerson() != null ? phoneNumber.getPerson().getId() : null,
                phoneNumber.getContact() != null ? phoneNumber.getContact().getId() : null,
                phoneNumber.getCreatedAt(),
                phoneNumber.getUpdatedAt()
        );
    }

    private String normalizeLanguage(String rawLanguage) {
        if (rawLanguage == null || rawLanguage.isBlank()) {
            return null;
        }

        String normalized = rawLanguage.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("fr")) {
            return "fr";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }

        return normalized;
    }
}
