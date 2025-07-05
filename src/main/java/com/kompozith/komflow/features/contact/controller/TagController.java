package com.kompozith.komflow.features.contact.controller;

import jakarta.validation.Valid;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kompozith.komflow.features.core.util.AppConstants.API_PREFIX_V1;

@RestController
@AllArgsConstructor
@RequestMapping(API_PREFIX_V1+"/tag")
public class TagController {

    private final TagService tagService;

    @PreAuthorize("hasAuthority('TAG_CREATE')")
    @PostMapping
    public TagDto create(@Valid @RequestBody TagDto tagDto) {
        return tagService.create(tagDto);
    }

    @PreAuthorize("hasAuthority('TAG_LIST')")
    @GetMapping
    public List<TagDto> findAll() {
        return tagService.findAll();
    }

    @PreAuthorize("hasAuthority('TAG_SHOW')")
    @GetMapping("/{id}")
    public TagDto findById(@PathVariable Long id) {
        return tagService.findById(id);
    }

    @PreAuthorize("hasAuthority('TAG_UPDATE')")
    @PutMapping("/{id}")
    public TagDto update(@PathVariable Long id, @Valid @RequestBody TagDto tagDto) {
        tagDto.setId(id);
        return tagService.update(id, tagDto);
    }

    @PreAuthorize("hasAuthority('TAG_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
