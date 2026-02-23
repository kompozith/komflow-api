package com.kompozith.komflow.features.contact.mapper;

import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.ContactDto;
import com.kompozith.komflow.features.contact.dto.CreateContactDto;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Primary
public class ContactMapperManual implements ContactMapper {

    @Override
    public Contact contactDtoToContact(ContactDto contactDto) {
        if (contactDto == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setId(contactDto.getId());
        contact.setEnabled(contactDto.isEnabled());
        contact.setLastMessageReceivedAt(contactDto.getLastMessageReceivedAt());
        return contact;
    }

    @Override
    public Contact createContactDtoToContact(CreateContactDto createContactDto) {
        if (createContactDto == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setEnabled(createContactDto.isEnabled());
        contact.setLastMessageReceivedAt(createContactDto.getLastMessageReceivedAt());
        return contact;
    }

    @Override
    public ContactDto contactToContactDto(Contact contact) {
        if (contact == null) {
            return null;
        }
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        dto.setEnabled(contact.isEnabled());
        dto.setLastMessageReceivedAt(contact.getLastMessageReceivedAt());
        dto.setPerson(personToPersonDto(contact.getPerson()));
        dto.setTagCount(contact.getTags() != null ? contact.getTags().size() : 0);
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());
        return dto;
    }

    @Override
    public ContactDetailsDto contactToContactDetailsDto(Contact contact) {
        if (contact == null) {
            return null;
        }
        ContactDetailsDto dto = new ContactDetailsDto();
        dto.setId(contact.getId());
        dto.setEnabled(contact.isEnabled());
        dto.setLastMessageReceivedAt(contact.getLastMessageReceivedAt());
        dto.setPerson(personToPersonDto(contact.getPerson()));
        dto.setTags(mapTags(contact.getTags()));
        dto.setCreatedAt(contact.getCreatedAt());
        dto.setUpdatedAt(contact.getUpdatedAt());
        return dto;
    }

    @Override
    public PersonDto personToPersonDto(Person person) {
        if (person == null) {
            return null;
        }
        String primaryPhone = null;
        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            primaryPhone = phoneNumbers.get(0).getNumber();
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

    private List<TagDto> mapTags(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream().map(this::mapTag).toList();
    }

    private TagDto mapTag(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDescription(tag.getDescription());
        dto.setColorCode(tag.getColorCode());
        dto.setEnabled(tag.isEnabled());
        dto.setContactCount(tag.getContacts() != null ? (long) tag.getContacts().size() : 0L);
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        return dto;
    }
}
