package com.komflow.kompozith.features.contact.controller;

import jakarta.validation.Valid;
import com.komflow.kompozith.features.contact.dto.TagDto;
import com.komflow.kompozith.features.contact.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.komflow.kompozith.features.core.util.AppConstants.API_PREFIX_V1;

@RestController
@AllArgsConstructor
@RequestMapping(API_PREFIX_V1+"tag")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasAuthority('user:read')")
    public TagDto create(@Valid @RequestBody TagDto tagDto) {
        return tagService.create(tagDto);
    }

    @GetMapping
    public List<TagDto> getAll() {
        return tagService.getAll();
    }

    @GetMapping("/{id}")
    public TagDto getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @PutMapping("/{id}")
    public TagDto update(@PathVariable Long id, @Valid @RequestBody TagDto tagDto) {
        tagDto.setId(id);
        return tagService.update(tagDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
