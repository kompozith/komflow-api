package com.kompozith.komflow.features.contact.mapper;

import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.ContactDto;
import com.kompozith.komflow.features.contact.dto.ContactWithTagCountDto;
import com.kompozith.komflow.features.contact.dto.ContactWithTagCountProjection;
import com.kompozith.komflow.features.contact.dto.CreateContactDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.personnel.dto.PersonDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = TagMapper.class)
public interface ContactMapper {

    ContactMapper INSTANCE = Mappers.getMapper(ContactMapper.class);

    @Mapping(target = "person", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Contact contactDtoToContact(ContactDto contactDto);

    @Mapping(target = "person", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Contact createContactDtoToContact(CreateContactDto createContactDto);

    ContactDto contactToContactDto(Contact contact);

    ContactDetailsDto contactToContactDetailsDto(Contact contact);

    PersonDto personToPersonDto(com.kompozith.komflow.features.personnel.entity.Person person);

    @Mapping(target = "person.id", source = "personId")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.firstName", source = "firstName")
    @Mapping(target = "person.lastName", source = "lastName")
    @Mapping(target = "person.language", source = "language")
    @Mapping(target = "person.country", source = "country")
    @Mapping(target = "person.city", source = "city")
    @Mapping(target = "person.timezone", source = "timezone")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    @Mapping(target = "person.createdAt", source = "personCreatedAt")
    @Mapping(target = "person.updatedAt", source = "personUpdatedAt")
    ContactWithTagCountDto projectionToContactWithTagCountDto(ContactWithTagCountProjection projection);

}
