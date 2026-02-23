package com.kompozith.komflow.features.core.controller;

import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.dto.FileBulkDeleteRequestDto;
import com.kompozith.komflow.features.core.dto.FileDeleteResultDto;
import com.kompozith.komflow.features.core.dto.FileListResponseDto;
import com.kompozith.komflow.features.core.dto.OrphanFileCleanupResultDto;
import com.kompozith.komflow.features.core.entity.FileMediaType;
import com.kompozith.komflow.features.core.service.FileStorageService;
import com.kompozith.komflow.util.SimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PreAuthorize("hasAnyAuthority('MESSAGE_CREATE', 'MESSAGE_UPDATE')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> upload(@RequestParam("file") MultipartFile file) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ResponseEntity.ok(fileStorageService.upload(file, baseUrl));
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping
    public ResponseEntity<FileListResponseDto> findAll(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FileMediaType mediaType,
            @RequestParam(defaultValue = "false") boolean orphanOnly) {
        return ResponseEntity.ok(fileStorageService.findAll(pageable, search, mediaType, orphanOnly));
    }

    @PreAuthorize("hasAuthority('MESSAGE_LIST')")
    @GetMapping("/orphans")
    public ResponseEntity<Page<FileDto>> findOrphans(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FileMediaType mediaType) {
        return ResponseEntity.ok(fileStorageService.findOrphans(pageable, search, mediaType));
    }

    @PreAuthorize("hasAuthority('MESSAGE_DELETE')")
    @DeleteMapping("/orphans")
    public ResponseEntity<SimpleResponse<OrphanFileCleanupResultDto>> deleteOrphans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FileMediaType mediaType) {
        OrphanFileCleanupResultDto result = fileStorageService.deleteOrphans(search, mediaType);
        return ResponseEntity.ok(SimpleResponse.success("Orphan media cleanup completed", result));
    }

    @PreAuthorize("hasAuthority('MESSAGE_DELETE')")
    @PostMapping("/bulk-delete")
    public ResponseEntity<SimpleResponse<FileDeleteResultDto>> bulkDelete(@Valid @RequestBody FileBulkDeleteRequestDto request) {
        FileDeleteResultDto result = fileStorageService.deleteFilesByIds(request.getFileIds(), request.isOrphanOnly());
        return ResponseEntity.ok(SimpleResponse.success("Bulk delete completed", result));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        Resource resource = fileStorageService.loadAsResource(id);
        String fileName = resource.getFilename() != null ? resource.getFilename() : "attachment";
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
