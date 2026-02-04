package com.kompozith.komflow.features.contact.service;
import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.*;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.ContactMapper;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.contact.service.ContactService;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.messaging.entity.Campaign;
import com.kompozith.komflow.features.messaging.repository.CampaignRepository;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import com.kompozith.komflow.features.personnel.repository.PhoneNumberRepository;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.query.sqm.tree.SqmNode.log;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl extends BaseService implements ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final PersonRepository personRepository;
    private final TagRepository tagRepository;
    private final PhoneNumberRepository phoneNumberRepository;
    private final CampaignRepository campaignRepository;

    @Override
    public ContactDto create(CreateContactDto createContactDto) {

        Contact contact = contactMapper.createContactDtoToContact(createContactDto);

        // Determine person source
        Person person = resolvePersonForCreate(createContactDto);
        contact.setPerson(person);

        // Set the tags
        if (createContactDto.getTagIds() != null && !createContactDto.getTagIds().isEmpty()) {
            contact.setTags(new HashSet<>(tagRepository.findAllById(createContactDto.getTagIds())));
        }

        return contactMapper.contactToContactDto(
                contactRepository.save(contact)
        );
    }

    @Override
    public List<ContactDto> findAll() {
        return contactRepository.findAll().stream().map(
                contactMapper::contactToContactDto
        ).collect(Collectors.toList());
    }

    @Override
    public Page<ContactWithTagCountDto> findAll(Pageable pageable, String search, Boolean enabled, Instant createdAtFrom, Instant createdAtTo, String tagIds) {
        // Handle empty tagIds list to avoid SQL IN () error
        if (tagIds != null && tagIds.isEmpty()) {
            tagIds = null;
        }
        return contactRepository.findWithFiltersAndTagCount(search, enabled, createdAtFrom, createdAtTo, tagIds, pageable);
    }

    @Override
    public ContactDetailsDto findById(Long id) {
        Contact contact = contactRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));
        return contactMapper.contactToContactDetailsDto(contact);
    }

    @Override
    public ContactDto update(Long id, CreateContactDto createContactDto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));

        Contact alreadyExistedContact = contactRepository.findByPersonId(createContactDto.getPersonId()).orElse(null);

        // Throw exception if the personId is already assigned to another contact
        if(createContactDto.getPersonId() != null) {
            if(alreadyExistedContact != null && !alreadyExistedContact.getId().equals(contact.getId()))
                throw new ObjectExistException(Contact.class.getSimpleName(), "personId", createContactDto.getPersonId().toString());
        }

        // Update fields
        contact.setEnabled(createContactDto.isEnabled());
        contact.setLastMessageReceivedAt(createContactDto.getLastMessageReceivedAt());

        // Update person if personId provided
        if (createContactDto.getPersonId() != null) {
            com.kompozith.komflow.features.personnel.entity.Person person = personRepository.findById(createContactDto.getPersonId())
                    .orElseThrow(() -> new ObjectNotFoundException(com.kompozith.komflow.features.personnel.entity.Person.class.getSimpleName(), createContactDto.getPersonId()));
            contact.setPerson(person);
        }

        // Update tags if tagIds provided
        if (createContactDto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(createContactDto.getTagIds()));
            contact.setTags(tags);
        }

        // Assuming BaseEntity handles updatedAt automatically
        Contact updatedContact = contactRepository.save(contact);
        return contactMapper.contactToContactDto(updatedContact);
    }

    @Override
    public void delete(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));

        contactRepository.deleteById(id);
    }

    private Person resolvePersonForCreate(CreateContactDto createContactDto) {
        if (createContactDto.getPersonId() != null) {
            // Verify that another contact didn't exist with the given personId.
            if (contactRepository.findByPersonId(createContactDto.getPersonId()).isPresent()) {
                throw new ObjectExistException(Contact.class.getSimpleName(), "personId", createContactDto.getPersonId().toString());
            }

            return personRepository.findById(createContactDto.getPersonId())
                    .orElseThrow(() -> new ObjectNotFoundException("Person", createContactDto.getPersonId()));
        }

        CreatePersonDto personDto = createContactDto.getPerson();
        if (personDto == null) {
            throw new IllegalArgumentException("contact.person.selection.invalid");
        }

        if (personRepository.findByEmail(personDto.getEmail()).isPresent()) {
            throw new ObjectExistException(Person.class.getSimpleName(), "email", personDto.getEmail());
        }

        Person person = new Person();
        person.setEmail(personDto.getEmail());
        person.setFirstName(personDto.getFirstName());
        person.setLastName(personDto.getLastName());
        person.setLanguage(personDto.getLanguage());

        if (createContactDto.getPhoneNumbers() != null && !createContactDto.getPhoneNumbers().isEmpty()) {
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            for (CreatePhoneNumberDto phoneDto : createContactDto.getPhoneNumbers()) {
                if (phoneDto.getNumber() == null || phoneDto.getNumber().isBlank()) {
                    continue;
                }

                phoneNumberRepository.findByNumber(phoneDto.getNumber()).ifPresent(existing -> {
                    throw new IllegalArgumentException("Phone number already exists: " + phoneDto.getNumber());
                });

                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setNumber(phoneDto.getNumber());
                phoneNumber.setIsWhatsapp(phoneDto.getIsWhatsapp() != null ? phoneDto.getIsWhatsapp().toString() : "false");
                phoneNumber.setPerson(person);
                phoneNumbers.add(phoneNumber);
            }

            person.setPhoneNumbers(phoneNumbers);
        }

        return personRepository.save(person);
    }
}
