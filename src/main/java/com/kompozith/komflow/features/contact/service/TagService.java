package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.features.contact.dto.TagDto;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagService {
    TagDto create (TagDto tag);
    List<TagDto> findAll();

    Page<TagDto> findAll(Pageable pageable, String search, String sort, String startDate, String endDate);
    TagDto findById(Long id); // Changed from int to Long
    TagDto update(Long id, TagDto tag);
    TagDto toggleStatus(Long id, boolean enabled);
    void delete(Long id); // Changed from int to Long
}
