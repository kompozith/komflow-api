package features.contact.mapper;

import features.contact.dto.TagDto;
import features.contact.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagMapper {

    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    @Mapping(target = "contacts", ignore = true)
    Tag tagDtoToTag(TagDto tagDto);

    @Mapping(target = "id", ignore = true)
    TagDto tagToTagDto(Tag tag);

}
