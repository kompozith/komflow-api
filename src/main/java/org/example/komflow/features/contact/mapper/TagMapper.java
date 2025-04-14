package org.example.komflow.features.contact.mapper;

import org.example.komflow.features.contact.dto.TagDto;
import org.example.komflow.features.contact.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagMapper {

    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "id", ignore = true)
    Tag tagDtoToTag(TagDto tagDto);

    TagDto tagToTagDto(Tag tag);

}
