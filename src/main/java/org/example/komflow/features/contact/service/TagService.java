package org.example.komflow.features.contact.service;

import org.example.komflow.features.contact.dto.TagDto;

import java.util.List;

public interface TagService {
    TagDto create (TagDto tag);
    List<TagDto> getAll();
    TagDto getById(Long id); // Changed from int to Long
    TagDto update(TagDto tag);
    void delete(Long id); // Changed from int to Long
}
