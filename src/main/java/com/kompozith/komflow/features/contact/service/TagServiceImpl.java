package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.dto.TagWithCountDto;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.core.service.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends BaseService implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public TagDto create(TagDto tagDto) {
        // Check if tag with same name already exists
        if (tagRepository.findByName(tagDto.getName()).isPresent()) {
            throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
        }

        Tag tag = tagMapper.tagDtoToTag(tagDto);
        tag.setEnabled(true); // Default to enabled
        Tag savedTag = tagRepository.save(tag);
        TagDto dto = tagMapper.tagToTagDto(savedTag);
        // Calculate contact count for new tag (should be 0)
        dto.setContactCount(0L);
        return dto;
    }

    @Override
    public List<TagDto> findAll() {
        List<Object[]> results = tagRepository.findAllWithContactCount();
        return results.stream()
                .map(result -> {
                    Tag tag = (Tag) result[0];
                    Long contactCount = (Long) result[1];
                    TagDto dto = tagMapper.tagToTagDto(tag);
                    dto.setContactCount(contactCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<TagWithCountDto> findAll(Pageable pageable, String search, Instant startDate, Instant endDate, Boolean enabled) {
        // Get all tags with contact count
        return tagRepository.findWithFiltersAndContactCount(search, startDate, endDate, enabled, pageable);
    }

    @Override
    public TagDto findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        TagDto dto = tagMapper.tagToTagDto(tag);
        // Calculate contact count for this tag
        dto.setContactCount(tag.getContacts() != null ? (long) tag.getContacts().size() : 0L);
        return dto;
    }

    @Override
    public TagDto update(Long id, TagDto tagDto) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));

        // Check if another tag with same name exists
        tagRepository.findByName(tagDto.getName()).ifPresent(tag -> {
            if (!tag.getId().equals(id)) {
                throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
            }
        });

        existingTag.setName(tagDto.getName());
        existingTag.setDescription(tagDto.getDescription());
        existingTag.setColorCode(tagDto.getColorCode());
        // Note: enabled field not in TagDto, assuming it's not updated via this method

        Tag updatedTag = tagRepository.save(existingTag);
        return tagMapper.tagToTagDto(updatedTag);
    }

    @Override
    public TagDto toggleStatus(Long id, boolean enabled) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));

        existingTag.setEnabled(enabled);
        Tag updatedTag = tagRepository.save(existingTag);
        return tagMapper.tagToTagDto(updatedTag);
    }

    @Override
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        tagRepository.deleteById(id);
    }
}
