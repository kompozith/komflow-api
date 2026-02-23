package com.kompozith.komflow.features.core.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.dto.FileDeleteResultDto;
import com.kompozith.komflow.features.core.dto.FileListResponseDto;
import com.kompozith.komflow.features.core.dto.OrphanFileCleanupResultDto;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.entity.FileMediaType;
import com.kompozith.komflow.features.core.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final FileRepository fileRepository;

    @Value("${app.file-storage.path:uploads}")
    private String storagePath;

    @Override
    public FileDto upload(MultipartFile multipartFile, String baseUrl) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalFilename = StringUtils.cleanPath(multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "file");
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("File name is required");
        }
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        File file = new File();
        file.setName(originalFilename);
        file.setUrl("pending");
        File savedFile = fileRepository.save(file);

        try {
            Path uploadDir = resolveAndCreateUploadDir();
            String extension = extractExtension(originalFilename);
            String storedFileName = savedFile.getId() + extension;
            Path target = uploadDir.resolve(storedFileName).normalize();
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            savedFile.setUrl(normalizedBaseUrl + "/files/" + savedFile.getId() + "/download");
            savedFile = fileRepository.save(savedFile);

            return toFileDto(savedFile);
        } catch (Exception e) {
            fileRepository.deleteById(savedFile.getId());
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadAsResource(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ObjectNotFoundException(File.class.getSimpleName(), fileId));

        try {
            Path uploadDir = resolveAndCreateUploadDir();
            String extension = extractExtension(file.getName());
            Path target = uploadDir.resolve(fileId + extension).normalize();
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ObjectNotFoundException("UploadedFile", fileId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to read stored file", e);
        }
    }

    @Override
    public FileListResponseDto findAll(Pageable pageable, String search, FileMediaType mediaType, boolean orphanOnly) {
        Pageable normalizedPageable = normalizePageableForNativeQuery(pageable);
        Page<File> files = fileRepository.findWithFilters(search, mediaType != null ? mediaType.name() : null, orphanOnly, normalizedPageable);
        Page<FileDto> fileDtos = files.map(this::toFileDto);
        Map<FileMediaType, Long> groupedByMediaType = buildGroupedByMediaType(search, orphanOnly);
        return new FileListResponseDto(fileDtos, groupedByMediaType);
    }

    @Override
    public Page<FileDto> findOrphans(Pageable pageable, String search, FileMediaType mediaType) {
        Pageable normalizedPageable = normalizePageableForNativeQuery(pageable);
        return fileRepository
                .findWithFilters(search, mediaType != null ? mediaType.name() : null, true, normalizedPageable)
                .map(this::toFileDto);
    }

    @Override
    public OrphanFileCleanupResultDto deleteOrphans(String search, FileMediaType mediaType) {
        int pageSize = 200;
        int deletedInDatabase = 0;
        int deletedInStorage = 0;
        List<Long> failedFileIds = new ArrayList<>();

        while (true) {
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<File> orphanPage = fileRepository.findWithFilters(search, mediaType != null ? mediaType.name() : null, true, pageable);
            if (orphanPage.isEmpty()) {
                break;
            }

            for (File file : orphanPage.getContent()) {
                try {
                    if (deleteStoredFileIfExists(file)) {
                        deletedInStorage++;
                    }
                    fileRepository.delete(file);
                    deletedInDatabase++;
                } catch (Exception e) {
                    failedFileIds.add(file.getId());
                    log.warn("Failed to delete orphan file {}: {}", file.getId(), e.getMessage());
                }
            }
        }

        return new OrphanFileCleanupResultDto(deletedInDatabase, deletedInStorage, failedFileIds);
    }

    @Override
    public FileDeleteResultDto deleteFilesByIds(List<Long> fileIds, boolean orphanOnly) {
        List<Long> normalizedIds = fileIds == null
                ? List.of()
                : fileIds.stream().filter(id -> id != null && id > 0).toList();

        if (normalizedIds.isEmpty()) {
            return new FileDeleteResultDto(0, 0, new ArrayList<>(), new ArrayList<>());
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(normalizedIds);
        List<File> existingFiles = fileRepository.findAllById(uniqueIds);
        Map<Long, File> byId = existingFiles.stream().collect(java.util.stream.Collectors.toMap(File::getId, f -> f));

        List<Long> skippedReferencedIds = orphanOnly
                ? fileRepository.findReferencedIds(new ArrayList<>(uniqueIds))
                : List.of();
        Set<Long> skippedReferencedSet = new LinkedHashSet<>(skippedReferencedIds);

        int deletedInDatabase = 0;
        int deletedInStorage = 0;
        List<Long> failedFileIds = new ArrayList<>();

        for (Long id : uniqueIds) {
            if (skippedReferencedSet.contains(id)) {
                continue;
            }

            File file = byId.get(id);
            if (file == null) {
                failedFileIds.add(id);
                continue;
            }

            try {
                if (deleteStoredFileIfExists(file)) {
                    deletedInStorage++;
                }
                fileRepository.delete(file);
                deletedInDatabase++;
            } catch (Exception e) {
                failedFileIds.add(id);
                log.warn("Failed to delete file {}: {}", id, e.getMessage());
            }
        }

        return new FileDeleteResultDto(
                deletedInDatabase,
                deletedInStorage,
                failedFileIds,
                new ArrayList<>(skippedReferencedSet)
        );
    }

    private Path resolveAndCreateUploadDir() {
        try {
            Path uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            return uploadDir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize upload directory", e);
        }
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot);
    }

    private FileDto toFileDto(File file) {
        return new FileDto(
                file.getId(),
                file.getName(),
                file.getUrl(),
                FileMediaType.fromFileName(file.getName()),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

    private Map<FileMediaType, Long> buildGroupedByMediaType(String search, boolean orphanOnly) {
        Map<FileMediaType, Long> grouped = new EnumMap<>(FileMediaType.class);
        for (FileMediaType mediaType : FileMediaType.values()) {
            grouped.put(mediaType, 0L);
        }

        List<Object[]> rows = fileRepository.countByMediaType(search, orphanOnly);
        for (Object[] row : rows) {
            FileMediaType mediaType = FileMediaType.valueOf(String.valueOf(row[0]));
            Long count = ((Number) row[1]).longValue();
            grouped.put(mediaType, count);
        }
        return grouped;
    }

    private boolean deleteStoredFileIfExists(File file) {
        try {
            Path uploadDir = resolveAndCreateUploadDir();
            String extension = extractExtension(file.getName());
            Path target = uploadDir.resolve(file.getId() + extension).normalize();
            return Files.deleteIfExists(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete stored file", e);
        }
    }

    private Pageable normalizePageableForNativeQuery(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> normalizedOrders = pageable.getSort().stream()
                .map(order -> new Sort.Order(order.getDirection(), mapSortProperty(order.getProperty())))
                .toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(normalizedOrders));
    }

    private String mapSortProperty(String property) {
        if (!StringUtils.hasText(property)) {
            return "created_at";
        }

        return switch (property) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            default -> property;
        };
    }
}
