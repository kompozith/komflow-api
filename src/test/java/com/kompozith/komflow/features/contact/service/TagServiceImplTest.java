package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.configuration.exception.ObjectExistException;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    Tag tag;
    TagDto tagDto;
    Tag expectedSavedTag;

    @BeforeEach
    void setUp() {

        // tagDto and mapped TagDto
        tagDto = new TagDto();
        tagDto.setId(1L);
        tagDto.setName("Tag Example");
        tagDto.setColorCode("#FFFFFFFF");
        tagDto.setDescription("Tag Example Description");

        // Tag and mapped tag
        tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag Example");
        tag.setColorCode("#FFFFFFFF");
        tag.setDescription("Tag Example Description");

        expectedSavedTag = new Tag();
        expectedSavedTag.setId(tagDto.getId());
        expectedSavedTag.setName(tagDto.getName());
        expectedSavedTag.setColorCode(tagDto.getColorCode());
        expectedSavedTag.setDescription(tagDto.getDescription());
    }

    @Test
    void shouldCreateTagSuccessfully() {

        when(tagRepository.findByName(tagDto.getName())).thenReturn(Optional.empty());
        when(tagMapper.tagDtoToTag(tagDto)).thenReturn(tag);
        when(tagRepository.save(any())).thenReturn(tag);
        when(tagMapper.tagToTagDto(any())).thenReturn(tagDto);

        TagDto savedTag = tagService.create(tagDto);

        assertNotNull(savedTag);
        assert(savedTag.getId().equals(tagDto.getId()));
        assert(savedTag.getName().equals(tagDto.getName()));
        assert(savedTag.getColorCode().equals(tagDto.getColorCode()));
        assert(savedTag.getDescription().equals(tagDto.getDescription()));
    }

    @Test
    void shouldReturnObjectExistExceptionOnCreateWhenTadExist() {

        when(tagRepository.findByName(tagDto.getName())).thenReturn(Optional.of(tag));

        ObjectExistException existException = assertThrows(ObjectExistException.class, () ->
                tagService.create(tagDto)
        );

        assertEquals("Tag already exists with name: " + tagDto.getName(), existException.getMessage());
        verify(tagRepository, never()).save(any());
        verifyNoInteractions(tagMapper);
    }

    @Test
    void getById() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}