package com.kompozith.komflow.features.contact.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Tag creation", description = "Return the new created tag.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully."),
            @ApiResponse(responseCode = "409", description = "The tag already exist with the given name.")
    })
    @PreAuthorize("hasAuthority('TAG_CREATE')")
    @PostMapping
    public TagDto create(@Valid @RequestBody TagDto tagDto) {
        return tagService.create(tagDto);
    }

    @Operation(summary = "Tags list", description = "Return the tag list.")
    @ApiResponse(responseCode = "200", description = "Tag list.")
    @PreAuthorize("hasAuthority('TAG_LIST')")
    @GetMapping
    public List<TagDto> findAll() {
        return tagService.findAll();
    }

    @Operation(summary = "Tag details", description = "Return a single tag when his Id is provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag details."),
            @ApiResponse(responseCode = "404", description = "Tag not found.")
    })
    @PreAuthorize("hasAuthority('TAG_SHOW')")
    @GetMapping("/{id}")
    public TagDto findById(@PathVariable Long id) {
        return tagService.findById(id);
    }

    @Operation(summary = "Tag update", description = "Return the updated tag.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag updated successfully."),
            @ApiResponse(responseCode = "404", description = "Tag not found."),
            @ApiResponse(responseCode = "409", description = "The tag already exist with the given name.")
    })
    @PreAuthorize("hasAuthority('TAG_UPDATE')")
    @PutMapping("/{id}")
    public TagDto update(@PathVariable Long id, @Valid @RequestBody TagDto tagDto) {
        tagDto.setId(id);
        return tagService.update(id, tagDto);
    }

    @Operation(summary = "Tag delete")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Tag not found."),
    })
    @PreAuthorize("hasAuthority('TAG_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
