package features.contact.service;

import features.contact.dto.TagDto;

import java.util.List;

public interface TagService {
    TagDto create (TagDto tag);
    List<TagDto> getAll();
    TagDto getById(int id);
    TagDto update(TagDto tag);
    void delete(int id);
}
