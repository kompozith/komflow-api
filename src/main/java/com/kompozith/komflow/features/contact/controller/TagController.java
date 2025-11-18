package com.kompozith.komflow.features.contact.controller;

import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/tags")
@Tag(name = "Tag Management", description = "APIs for managing tags")
public class TagController {

    private final TagService tagService;

    @PreAuthorize("hasAuthority('TAG_CREATE')")
    @PostMapping
    @Operation(summary = "Create a new tag", description = "Create a new tag in the system")
    public TagDto create(@Valid @RequestBody TagDto tagDto) {
        return tagService.create(tagDto);
    }

    @PreAuthorize("hasAuthority('TAG_LIST')")
    @GetMapping
    @Operation(summary = "Get all tags", description = "Retrieve a list of all tags")
    public List<TagDto> findAll() {
        return tagService.findAll();
    }

    @PreAuthorize("hasAuthority('TAG_SHOW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Retrieve a specific tag by its ID")
    public TagDto findById(@PathVariable Long id) {
        return tagService.findById(id);
    }

    @PreAuthorize("hasAuthority('TAG_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update tag", description = "Update an existing tag by its ID")
    public TagDto update(@PathVariable Long id, @Valid @RequestBody TagDto tagDto) {
        tagDto.setId(id);
        return tagService.update(id, tagDto);
    }

    @PreAuthorize("hasAuthority('TAG_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag", description = "Delete a tag by its ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
