package org.example.komflow.features.contact.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.komflow.features.contact.dto.TagDto;
import org.example.komflow.features.contact.entity.Tag;
import org.example.komflow.features.contact.mapper.TagMapper;
import org.example.komflow.features.contact.repository.TagRepository;
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
    private TagMapper tagMapper; // Mocking the interface

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tagEntity;
    private TagDto tagDto;

    @BeforeEach
    void setUp() {
        tagEntity = new Tag();
        tagEntity.setId(1L);
        tagEntity.setName("Test Tag");
        tagEntity.setColorCode("#FFFFFF");
        tagEntity.setDescription("Test Description");

        tagDto = new TagDto();
        tagDto.setId(1L);
        tagDto.setName("Test Tag DTO");
        tagDto.setColorCode("#000000");
        tagDto.setDescription("Test DTO Description");
    }

    // --- getById Tests ---
    @Test
    void getById_whenTagExists_shouldReturnTagDto() {
        // Arrange
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tagEntity));
        when(tagMapper.tagToTagDto(tagEntity)).thenReturn(tagDto);

        // Act
        TagDto result = tagService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(tagDto.getId(), result.getId());
        assertEquals(tagDto.getName(), result.getName());
        verify(tagRepository).findById(1L);
        verify(tagMapper).tagToTagDto(tagEntity);
    }

    @Test
    void getById_whenTagNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            tagService.getById(1L);
        });
        verify(tagRepository).findById(1L);
        verifyNoInteractions(tagMapper);
    }

    // --- update Tests ---
    @Test
    void update_whenTagExists_shouldUpdateAndReturnTagDto() {
        // Arrange
        Tag existingTag = new Tag(); // Simulate existing entity
        existingTag.setId(1L);
        existingTag.setName("Old Name");
        existingTag.setColorCode("#OLDCLR");
        
        TagDto inputDto = new TagDto();
        inputDto.setId(1L);
        inputDto.setName("New Name");
        inputDto.setColorCode("#NEWCLR");
        inputDto.setDescription("New Desc");

        Tag savedTag = new Tag(); // Simulate entity after save
        savedTag.setId(1L);
        savedTag.setName(inputDto.getName());
        savedTag.setColorCode(inputDto.getColorCode());
        savedTag.setDescription(inputDto.getDescription());
        
        TagDto expectedReturnDto = new TagDto(); // DTO mapped from savedTag
        expectedReturnDto.setId(1L);
        expectedReturnDto.setName(savedTag.getName());
        // Ensure all relevant fields are set for the expected DTO
        expectedReturnDto.setColorCode(savedTag.getColorCode());
        expectedReturnDto.setDescription(savedTag.getDescription());


        when(tagRepository.findById(1L)).thenReturn(Optional.of(existingTag));
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(tagMapper.tagToTagDto(savedTag)).thenReturn(expectedReturnDto);

        // Act
        TagDto result = tagService.update(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedReturnDto.getName(), result.getName());
        assertEquals(expectedReturnDto.getColorCode(), result.getColorCode());
        assertEquals(expectedReturnDto.getDescription(), result.getDescription());
        
        verify(tagRepository).findById(1L);
        verify(tagRepository).save(argThat(tag -> 
            tag.getId().equals(1L) &&
            tag.getName().equals(inputDto.getName()) &&
            tag.getColorCode().equals(inputDto.getColorCode()) &&
            tag.getDescription().equals(inputDto.getDescription())
        ));
        verify(tagMapper).tagToTagDto(savedTag);
    }
    
    @Test
    void update_whenTagNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        TagDto inputDto = new TagDto();
        inputDto.setId(1L); // ID of non-existent tag
        inputDto.setName("Attempt Update Name");
        
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            tagService.update(inputDto);
        });
        
        verify(tagRepository).findById(1L);
        verify(tagRepository, never()).save(any(Tag.class));
        verifyNoInteractions(tagMapper);
    }

    // --- delete Tests ---
    @Test
    void delete_whenTagExists_shouldCallDeleteById() {
        // Arrange
        when(tagRepository.existsById(1L)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(1L); // For void methods

        // Act
        tagService.delete(1L);

        // Assert
        verify(tagRepository).existsById(1L);
        verify(tagRepository).deleteById(1L);
    }

    @Test
    void delete_whenTagNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        when(tagRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            tagService.delete(1L);
        });
        
        verify(tagRepository).existsById(1L);
        verify(tagRepository, never()).deleteById(1L);
    }
}
