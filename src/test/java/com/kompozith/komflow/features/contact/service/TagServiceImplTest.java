package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.configuration.exception.ObjectExistException;
import com.kompozith.komflow.configuration.exception.ObjectNotFoundException;
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
    void shouldCreateSuccessfully() {

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

        verify(tagRepository).findByName(tagDto.getName());
        verify(tagMapper).tagDtoToTag(tagDto);
        verify(tagRepository, times(1)).save(any());
        verify(tagMapper, times(1)).tagToTagDto(tag);
    }

    @Test
    void shouldReturnObjectExistExceptionOnCreateWhenAlreadyExistWithGivenName() {

        when(tagRepository.findByName(tagDto.getName())).thenReturn(Optional.of(tag));

        ObjectExistException existException = assertThrows(ObjectExistException.class, () ->
                tagService.create(tagDto)
        );

        assertEquals("Tag already exists with name " + tagDto.getName() + ".", existException.getMessage());

        verify(tagRepository).findByName(tagDto.getName());
        verify(tagRepository, never()).save(any());
        verifyNoInteractions(tagMapper);
    }

    @Test
    void shouldFindByIdSuccessfullyWhenTagExist() {
        when(tagRepository.findById(tagDto.getId())).thenReturn(Optional.of(tag));
        when(tagMapper.tagToTagDto(any())).thenReturn(tagDto);

        TagDto foundTag = tagService.findById(tagDto.getId());

        assertNotNull(foundTag);
        assert(foundTag.getId().equals(tag.getId()));
        assert(foundTag.getName().equals(tag.getName()));
        assert(foundTag.getColorCode().equals(tagDto.getColorCode()));
        assert(foundTag.getDescription().equals(tagDto.getDescription()));

        verify(tagRepository).findById(tag.getId());
        verify(tagMapper).tagToTagDto(tag);
    }

    @Test
    void shouldReturnObjectNotFoundExceptionOnFindByIdWhenTagNotExistWithGivenId() {
        when(tagRepository.findById(any())).thenReturn(Optional.empty());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class, () ->
                tagService.findById(tagDto.getId())
        );

        assertEquals("Tag not found with id " + tagDto.getId() + ".", objectNotFoundException.getMessage());

        verify((tagRepository)).findById(tagDto.getId());
        verifyNoInteractions(tagMapper);
    }

    @Test
    void shouldUpdateSuccessfully() {

        // Dto information for tag to update
        TagDto updatedTagDto = new TagDto();
        updatedTagDto.setName("Updated tag");
        updatedTagDto.setColorCode("#TE7FFF");
        updatedTagDto.setDescription("Updated description");

        // saved updated tag
        Tag updatedTag = new Tag();
        updatedTag.setName(updatedTagDto.getName());
        updatedTag.setColorCode(updatedTagDto.getColorCode());
        updatedTag.setDescription(updatedTagDto.getDescription());

        when(tagRepository.findById(tagDto.getId())).thenReturn(Optional.of(tag));
        when(tagRepository.save(any())).thenReturn(updatedTag);
        when(tagMapper.tagToTagDto(any())).thenReturn(updatedTagDto);

        TagDto savedUpdatedTag = tagService.update(tagDto.getId(), updatedTagDto);

        assertNotNull(savedUpdatedTag);
        assertEquals(savedUpdatedTag.getName(), updatedTagDto.getName());
        assertEquals(savedUpdatedTag.getColorCode(), updatedTagDto.getColorCode());
        assertEquals(savedUpdatedTag.getDescription(), updatedTagDto.getDescription());

        verify(tagRepository).save(argThat(savingtag ->
                savedUpdatedTag.getName().equals(updatedTagDto.getName()) &&
                savedUpdatedTag.getColorCode().equals(updatedTagDto.getColorCode()) &&
                savedUpdatedTag.getDescription().equals(updatedTagDto.getDescription())
        ));
        verify(tagRepository).findById(tagDto.getId());
        verify(tagMapper).tagToTagDto(updatedTag);
    }


    @Test
    void shouldReturnObjectNotFoundExceptionWhenUpdatingByNotFoundId() {

        when(tagRepository.findById(any())).thenReturn(Optional.empty());

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class, () ->
                tagService.update(tagDto.getId(), tagDto)
        );

        assertEquals("Tag not found with id " + tagDto.getId() + ".", objectNotFoundException.getMessage());
        verify(tagRepository, never()).save(any());
        verifyNoInteractions(tagMapper);
    }

    @Test
    void shouldDeleteTagSuccessfully() {
        when(tagRepository.existsById(tagDto.getId())).thenReturn(true);
        doNothing().when(tagRepository).deleteById(tagDto.getId());

        tagService.delete(tagDto.getId());

        verify(tagRepository).existsById(tagDto.getId());
        verify(tagRepository).deleteById(tagDto.getId());
    }

    @Test
    void shouldReturnObjectNotFoundExceptionWhenDeletingByNotFoundId() {
        when(tagRepository.existsById(tagDto.getId())).thenReturn(false);

        ObjectNotFoundException objectNotFoundException = assertThrows(ObjectNotFoundException.class, () ->
                tagService.delete(tagDto.getId())
        );

        assertEquals("Tag not found with id " + tagDto.getId() + ".", objectNotFoundException.getMessage());
        verify(tagRepository).existsById(tagDto.getId());
        verify(tagRepository, never()).deleteById(tagDto.getId());
    }
}