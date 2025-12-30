package com.kompozith.komflow.features.contact.mapper;

import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.dto.TagWithContactCountDto;
import com.kompozith.komflow.features.contact.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "id", ignore = true)
    Tag tagDtoToTag(TagDto tagDto);

    TagDto tagToTagDto(Tag tag);

    TagWithContactCountDto tagToTagWithContactCountDto(Tag tag);

}
