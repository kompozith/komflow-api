package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.features.contact.dto.TagDto;

import java.util.List;

public interface TagService {
    TagDto create (TagDto tag);
    List<TagDto> findAll();
    TagDto findById(Long id); // Changed from int to Long
    TagDto update(Long id, TagDto tag);
    void delete(Long id); // Changed from int to Long
}
