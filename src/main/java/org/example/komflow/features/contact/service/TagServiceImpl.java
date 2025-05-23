package org.example.komflow.features.contact.service;

import org.example.komflow.features.contact.dto.TagDto;
import org.example.komflow.features.contact.entity.Tag;
import org.example.komflow.features.contact.mapper.TagMapper;
import org.example.komflow.features.contact.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException; // Added import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public TagDto create(TagDto tagDto) {
        Tag tag = TagMapper.INSTANCE.tagDtoToTag(tagDto);
        return TagMapper.INSTANCE.tagToTagDto(
                tagRepository.save(tag)
        );
    }

    @Override
    public List<TagDto> getAll() {
        return tagRepository.findAll().stream().map(
                TagMapper.INSTANCE::tagToTagDto
        ).collect(Collectors.toList());
    }

    @Override
    public TagDto getById(Long id) { // Changed from int to Long
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        return TagMapper.INSTANCE.tagToTagDto(tag);
    }

    @Override
    public TagDto update(TagDto tagDto) {
        Long id = tagDto.getId();
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));

        // Update fields
        tag.setName(tagDto.getName());
        tag.setDescription(tagDto.getDescription());
        tag.setColorCode(tagDto.getColorCode());
        // Assuming BaseEntity handles updatedAt automatically

        Tag updatedTag = tagRepository.save(tag);
        return TagMapper.INSTANCE.tagToTagDto(updatedTag);
    }

    @Override
    public void delete(Long id) { // Changed from int to Long
        if (!tagRepository.existsById(id)) {
            throw new EntityNotFoundException("Tag not found with id: " + id);
        }
        tagRepository.deleteById(id);
    }
}
