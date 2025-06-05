package org.example.komflow.features.contact.controller;

import jakarta.validation.Valid;
import org.example.komflow.features.contact.dto.TagDto;
import org.example.komflow.features.contact.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.komflow.features.util.AppConstants.API_PREFIX_V1;

@RestController
@AllArgsConstructor
@RequestMapping(API_PREFIX_V1+"tag")
public class TagController {

    private final TagService tagService;

    @PostMapping
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
