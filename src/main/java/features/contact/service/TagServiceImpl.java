package features.contact.service;

import features.contact.dto.TagDto;
import features.contact.entity.Tag;
import features.contact.mapper.TagMapper;
import features.contact.repository.TagRepositoty;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private TagRepositoty tagRepositoty;
    public TagDto create(TagDto tagDto) {
        Tag tag = TagMapper.INSTANCE.tagDtoToTag(tagDto);
        return TagMapper.INSTANCE.tagToTagDto(
                tagRepositoty.save(tag)
        );
    }

    public List<TagDto> getAll() {
        return tagRepositoty.findAll().stream().map(
                TagMapper.INSTANCE::tagToTagDto
        ).collect(Collectors.toList());
    }

    public TagDto getById(int id) {
        return null;
    }

    public TagDto update(TagDto tagDto) {
        return null;
    }

    public void delete(int id) {

    }
}
