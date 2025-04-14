package org.example.komflow.features.contact.controller;

import org.example.komflow.features.contact.dto.TagDto;
import org.example.komflow.features.contact.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/tag")
public class TagController {

    private final TagService tagService;

    @PostMapping
    public TagDto create(TagDto tagDto) {
        return tagService.create(tagDto);
    }

    @GetMapping
    public List<TagDto> getAll() {
        return tagService.getAll();
    }
}
