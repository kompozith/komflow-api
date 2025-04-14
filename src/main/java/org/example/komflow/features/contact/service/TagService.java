package org.example.komflow.features.contact.service;

import org.example.komflow.features.contact.dto.TagDto;

import java.util.List;

public interface TagService {
    TagDto create (TagDto tag);
    List<TagDto> getAll();
    TagDto getById(int id);
    TagDto update(TagDto tag);
    void delete(int id);
}
