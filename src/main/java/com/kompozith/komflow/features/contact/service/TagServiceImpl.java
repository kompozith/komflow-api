package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.configuration.exception.ObjectExistException;
import com.kompozith.komflow.configuration.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.core.service.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends BaseService implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    public TagDto create(TagDto tagDto) {

        // Verify that another tag didn't exist with yhe given name.
        if(tagRepository.findByName(tagDto.getName()).isPresent()){
            throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
        }

        Tag tag = tagMapper.tagDtoToTag(tagDto);
        return tagMapper.tagToTagDto(
                tagRepository.save(tag)
        );
    }

    @Override
    public List<TagDto> findAll() {
        return tagRepository.findAll().stream().map(
                tagMapper::tagToTagDto
        ).collect(Collectors.toList());
    }

    @Override
    public TagDto findById(Long id) { // Changed from int to Long
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        return tagMapper.tagToTagDto(tag);
    }

    @Override
    public TagDto update(Long id, TagDto tagDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));

        Tag alredyExistedTag = tagRepository.findByName(tagDto.getName()).orElse(null);

        // Throw exception if the name is already assigned to another tag
        if(tagDto.getName() != null) {
            if(alredyExistedTag != null && !alredyExistedTag.getId().equals(tag.getId()))
                throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
        }

        // Update fields
        tag.setName(tagDto.getName());
        tag.setDescription(tagDto.getDescription());
        tag.setColorCode(tagDto.getColorCode());

        // Assuming BaseEntity handles updatedAt automatically
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.tagToTagDto(updatedTag);
    }

    @Override
    public void delete(Long id) { // Changed from int to Long
        if (!tagRepository.existsById(id)) {
            throw new ObjectNotFoundException(Tag.class.getSimpleName(), id);
        }
        tagRepository.deleteById(id);
    }
}